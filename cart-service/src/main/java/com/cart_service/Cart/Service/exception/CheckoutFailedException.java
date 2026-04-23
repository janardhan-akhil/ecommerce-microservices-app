package com.cart_service.Cart.Service.exception;

public class CheckoutFailedException extends RuntimeException {

    public CheckoutFailedException(Long userId) {
        super("Checkout failed for userId: " + userId
                + ". Order service may be unavailable. Please try again.");
    }

    public CheckoutFailedException(String message) {
        super(message);
    }
}
