package com.user_service.User.Service.service;


import com.user_service.User.Service.dto.request.LoginRequest;
import com.user_service.User.Service.dto.request.RegisterRequest;
import com.user_service.User.Service.dto.response.AuthResponse;

public interface AuthService {

    /** Register a new user and return a JWT immediately (no separate login needed). */
    AuthResponse register(RegisterRequest request);

    /** Validate credentials and return a signed JWT. */
    AuthResponse login(LoginRequest request);
}
