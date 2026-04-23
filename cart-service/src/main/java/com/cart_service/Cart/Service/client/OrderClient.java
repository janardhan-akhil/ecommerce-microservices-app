package com.cart_service.Cart.Service.client;

import com.cart_service.Cart.Service.dto.request.CreateOrderRequest;
import com.cart_service.Cart.Service.external.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {

    @PostMapping("/api/v1/orders")
    OrderResponse createOrder(@RequestBody CreateOrderRequest request);

}