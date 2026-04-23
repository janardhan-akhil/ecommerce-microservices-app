package com.cart_service.Cart.Service.external;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String status;
    private double totalAmount;
    private String shippingAddress;
    private LocalDateTime createdAt;
}
