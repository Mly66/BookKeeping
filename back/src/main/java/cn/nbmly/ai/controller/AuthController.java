package cn.nbmly.ai.controller;

import cn.nbmly.ai.dto.AuthResponse;
import cn.nbmly.ai.dto.LoginRequest;
import cn.nbmly.ai.dto.RegisterRequest;
import cn.nbmly.ai.entity.User;
import cn.nbmly.ai.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.register(request);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            String token = userService.login(request);
            User user = userService.findByUsername(request.getUsername());
            return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getEmail()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("无效的授权头");
            }

            String token = authHeader.substring(7);
            String newToken = userService.refreshToken(token);
            return ResponseEntity.ok(new AuthResponse(newToken, null, null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}