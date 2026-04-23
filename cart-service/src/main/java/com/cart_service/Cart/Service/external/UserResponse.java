package com.cart_service.Cart.Service.external;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;    // User.name
    private String email;
    private String role;
}
