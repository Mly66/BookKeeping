package cn.nbmly.ai.service;

import cn.nbmly.ai.dto.LoginRequest;
import cn.nbmly.ai.dto.RegisterRequest;
import cn.nbmly.ai.dto.UpdateUserRequest;
import cn.nbmly.ai.dto.UserDTO;
import cn.nbmly.ai.entity.User;

public interface UserService {
    User register(RegisterRequest registerRequest);

    String login(LoginRequest loginRequest);

    String refreshToken(String token);

    User findByUsername(String username);

    User findById(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    UserDTO updateUserProfile(Long userId, UpdateUserRequest request);
}