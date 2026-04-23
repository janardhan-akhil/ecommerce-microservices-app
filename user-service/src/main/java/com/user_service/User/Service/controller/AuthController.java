package com.user_service.User.Service.controller;


import com.user_service.User.Service.dto.request.LoginRequest;
import com.user_service.User.Service.dto.request.RegisterRequest;
import com.user_service.User.Service.dto.response.AuthResponse;
import com.user_service.User.Service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/users/register
     * Public — no JWT required (configured in gateway.public-paths).
     * Creates the user, hashes the password, returns a JWT immediately.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/users/register - {}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/users/login
     * Public — no JWT required (configured in gateway.public-paths).
     * Validates credentials and returns a signed JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/users/login - {}", request.getEmail());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/users/me
     * Protected — requires a valid JWT.
     * The gateway injects X-User-Id, X-User-Email, X-User-Role headers
     * before the request reaches here, so no token parsing is needed.
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @RequestHeader("X-User-Id")    String userId,
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Role")  String role) {

        return ResponseEntity.ok(Map.of(
                "userId", userId,
                "email",  email,
                "role",   role
        ));
    }
}
