package com.cart_service.Cart.Service.dto.response;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {
    private Long userId;
    private String customerName;
    private List<CartItemResponse> items;
    private int totalItems;         // distinct product lines
    private int totalQuantity;      // sum of all quantities
    private double totalAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
