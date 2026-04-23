package com.payment_service.Payment.Service.dto.response;


import lombok.*;

// Matches User entity: id, name, email, role
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
}
