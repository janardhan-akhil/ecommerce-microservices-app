package com.user_service.User.Service.service.impl;


import com.user_service.User.Service.dto.request.LoginRequest;
import com.user_service.User.Service.dto.request.RegisterRequest;
import com.user_service.User.Service.dto.response.AuthResponse;
import com.user_service.User.Service.entity.User;
import com.user_service.User.Service.repository.UserRepository;
import com.user_service.User.Service.service.AuthService;
import com.user_service.User.Service.utility.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    // BCrypt encoder — not a Spring Security bean, kept local to auth
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ── Register ─────────────────────────────────────────────────────

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already in use: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : "CUSTOMER");
        user.setCreated_at(new Date());

        User saved = userRepository.save(user);
        log.info("User registered: id={} email={}", saved.getId(), saved.getEmail());

        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail(), saved.getRole());
        return buildResponse(saved, token);
    }

    // ── Login ────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Invalid password for: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole());
        log.info("Login successful: userId={}", user.getId());
        return buildResponse(user, token);
    }

    // ── Private ──────────────────────────────────────────────────────

    private AuthResponse buildResponse(User user, String token) {
        return AuthResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .tokenType("Bearer")
                .expiresInMs(expirationMs)
                .build();
    }
}