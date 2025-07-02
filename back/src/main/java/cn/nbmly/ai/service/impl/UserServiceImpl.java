package cn.nbmly.ai.service.impl;

import cn.nbmly.ai.dto.LoginRequest;
import cn.nbmly.ai.dto.RegisterRequest;
import cn.nbmly.ai.dto.UpdateUserRequest;
import cn.nbmly.ai.dto.UserDTO;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.repository.UserRepository;
import cn.nbmly.ai.service.UserService;
import cn.nbmly.ai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public User register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());

        return userRepository.save(user);
    }

    @Override
    public String login(LoginRequest loginRequest) {
        // 查找用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 生成JWT token
        String token = jwtUtil.generateToken(user.getUsername());

        // 将token存入Redis，设置过期时间
        redisTemplate.opsForValue().set(
                "token:" + user.getUsername(),
                token,
                30,
                TimeUnit.MINUTES);

        return token;
    }

    @Override
    public String refreshToken(String token) {
        try {
            // 从令牌中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);

            // 检查用户是否存在
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 检查Redis中是否存在该令牌（可选的安全检查）
            String storedToken = redisTemplate.opsForValue().get("token:" + username);
            if (storedToken == null || !storedToken.equals(token)) {
                throw new RuntimeException("令牌已失效");
            }

            // 生成新的JWT token
            String newToken = jwtUtil.generateToken(username);

            // 更新Redis中的token
            redisTemplate.opsForValue().set(
                    "token:" + username,
                    newToken,
                    30,
                    TimeUnit.MINUTES);

            return newToken;
        } catch (Exception e) {
            throw new RuntimeException("令牌刷新失败: " + e.getMessage());
        }
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误");
        }

        // 设置新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 如果邮箱被修改，检查新邮箱是否已被其他人使用
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            Optional<User> userWithNewEmail = userRepository.findByEmail(request.getEmail());
            if (userWithNewEmail.isPresent() && !userWithNewEmail.get().getId().equals(userId)) {
                throw new RuntimeException("该邮箱已被其他账户使用");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        User updatedUser = userRepository.save(user);
        return toDto(updatedUser);
    }

    private UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setCreateTime(user.getCreateTime());
        return dto;
    }
}