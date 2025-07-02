package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.ChangePasswordRequest;
import cn.nbmly.ai.dto.UpdateUserRequest;
import cn.nbmly.ai.dto.UserDTO;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        try {
            userService.changePassword(currentUser.getId(), request.getOldPassword(), request.getNewPassword());
            return ResponseEntity.ok("密码修改成功");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getUserProfile(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        UserDTO userDTO = new UserDTO();
        userDTO.setId(currentUser.getId());
        userDTO.setUsername(currentUser.getUsername());
        userDTO.setNickname(currentUser.getNickname());
        userDTO.setEmail(currentUser.getEmail());
        userDTO.setPhone(currentUser.getPhone());
        userDTO.setAvatar(currentUser.getAvatar());
        userDTO.setCreateTime(currentUser.getCreateTime());
        return ResponseEntity.ok(userDTO);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(@Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        try {
            UserDTO updatedUser = userService.updateUserProfile(currentUser.getId(), request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}