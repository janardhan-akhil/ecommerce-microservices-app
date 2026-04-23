package com.cart_service.Cart.Service.external;


import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private double price;           // Product.price  (double)
    private int quantity;           // Product.quantity (int) — stock level
    private boolean available;
    private String category;
    private String brand;
    private String imageName;
    private LocalDateTime createdDate;
}
