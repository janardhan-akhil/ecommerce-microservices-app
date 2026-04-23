package com.order_service.Order.Service.client;

import com.order_service.Order.Service.external.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
