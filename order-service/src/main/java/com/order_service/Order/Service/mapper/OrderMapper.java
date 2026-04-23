package com.order_service.Order.Service.mapper;


import com.order_service.Order.Service.dto.response.OrderItemResponse;
import com.order_service.Order.Service.dto.response.OrderResponse;
import com.order_service.Order.Service.entity.Order;
import com.order_service.Order.Service.entity.OrderItem;
import com.order_service.Order.Service.external.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderResponse toOrderResponse(Order order, UserResponse user) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .customerName(user != null ? user.getName() : null)   // User.name
                .customerEmail(user != null ? user.getEmail() : null)
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .paymentMethod(order.getPaymentMethod())
                .orderItems(toOrderItemResponses(order.getOrderItems()))
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    public OrderResponse toOrderResponse(Order order) {
        return toOrderResponse(order, null);
    }

    public List<OrderItemResponse> toOrderItemResponses(List<OrderItem> items) {
        return items.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    public OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}