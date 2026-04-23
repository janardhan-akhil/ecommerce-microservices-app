package com.cart_service.Cart.Service.entity;


import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem implements Serializable {

    private Long productId;
    private String productName;
    private String category;
    private String brand;
    private String imageName;

    // Product.price is double — kept as double for consistency
    private double unitPrice;
    private int quantity;
    private double totalPrice;  // unitPrice * quantity

    /** Recalculate totalPrice from current unitPrice and quantity. */
    public void recalculate() {
        this.totalPrice = this.unitPrice * this.quantity;
    }
}