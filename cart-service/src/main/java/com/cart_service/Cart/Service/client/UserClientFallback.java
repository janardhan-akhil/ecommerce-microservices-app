package com.cart_service.Cart.Service.client;


import com.cart_service.Cart.Service.external.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserClientFallback implements UserClient {



    @Override
    public UserResponse getUserById(Long id) {
        log.warn("UserService is unavailable. Returning fallback for userId: {}", id);
        return UserResponse.builder()
                .id(id)
                .name("Unknown User")
                .email("unknown@fallback.com")
                .role("CUSTOMER")
                .build();
    }
}
