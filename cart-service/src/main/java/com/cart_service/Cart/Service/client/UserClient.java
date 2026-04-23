package com.cart_service.Cart.Service.client;


import com.cart_service.Cart.Service.external.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service",fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/api/v1/users/{id}")
    public UserResponse getUserById(@PathVariable("id") Long id);
}
