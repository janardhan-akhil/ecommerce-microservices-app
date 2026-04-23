package com.user_service.User.Service.dto.response;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;           // JWT Bearer token
    private String tokenType;       // always "Bearer"
    private long expiresInMs;       // token lifetime in ms
}
