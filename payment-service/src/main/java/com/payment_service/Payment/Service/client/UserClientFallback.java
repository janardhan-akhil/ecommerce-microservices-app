package com.payment_service.Payment.Service.client;


import com.payment_service.Payment.Service.dto.response.UserResponse;
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
