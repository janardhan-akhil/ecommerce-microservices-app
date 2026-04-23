package com.cart_service.Cart.Service.dto.response;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long productId;
    private String productName;
    private String category;
    private String brand;
    private String imageName;
    private double unitPrice;
    private int quantity;
    private double totalPrice;
}
