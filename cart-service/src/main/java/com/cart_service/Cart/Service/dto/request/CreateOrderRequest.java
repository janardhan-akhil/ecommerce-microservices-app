package com.cart_service.Cart.Service.dto.request;


import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    private Long userId;
    private String shippingAddress;
    private String billingAddress;
    private String paymentMethod;
    private String notes;
    private List<OrderItemRequest> items;
}
