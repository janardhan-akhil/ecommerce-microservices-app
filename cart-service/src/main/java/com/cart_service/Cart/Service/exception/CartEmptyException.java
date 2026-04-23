package com.cart_service.Cart.Service.exception;


public class CartEmptyException extends RuntimeException {

    public CartEmptyException(Long userId) {
        super("Cannot checkout: cart is empty for userId: " + userId);
    }
}
