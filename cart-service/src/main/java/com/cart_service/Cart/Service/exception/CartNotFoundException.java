package com.cart_service.Cart.Service.exception;


public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException(Long userId) {
        super("Cart not found for userId: " + userId);
    }

    public CartNotFoundException(String message) {
        super(message);
    }
}
