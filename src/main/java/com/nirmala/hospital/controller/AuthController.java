package com.nirmala.hospital.controller;

import com.nirmala.hospital.model.Admin;
import com.nirmala.hospital.repository.AdminRepository;
import com.nirmala.hospital.security.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil,
                          AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenUtil.generateToken(request.getEmail());

            Admin admin = adminRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("Admin not found"));

            admin.setLastLogin(LocalDateTime.now());
            adminRepository.save(admin);

            // Set secure cookie
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // In dev, we can set to false. For prod HTTPS, set to true.
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60); // 1 day
            response.addCookie(cookie);

            Map<String, Object> body = new HashMap<>();
            body.put("token", token);
            body.put("name", admin.getName());
            body.put("email", admin.getEmail());
            body.put("role", admin.getRole());

            return ResponseEntity.ok(body);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("token", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Map<String, String> body = new HashMap<>();
        body.put("message", "Logged out successfully");
        return ResponseEntity.ok(body);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }

        Admin admin = adminRepository.findByEmail(auth.getName()).orElse(null);
        if (admin == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", admin.getName());
        body.put("email", admin.getEmail());
        body.put("role", admin.getRole());
        return ResponseEntity.ok(body);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        private String email;
        private String password;
    }
}
