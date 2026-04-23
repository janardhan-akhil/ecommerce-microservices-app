package com.cart_service.Cart.Service.client;


import com.cart_service.Cart.Service.dto.request.CreateOrderRequest;
import com.cart_service.Cart.Service.external.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.error("OrderService unavailable — checkout failed for userId: {}", request.getUserId());
        // Return null so the service layer can detect the failure and throw a meaningful exception
        return null;
    }
}