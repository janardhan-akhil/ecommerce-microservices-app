package com.payment_service.Payment.Service.exception;


public class PaymentAlreadyExistsException extends RuntimeException {
    public PaymentAlreadyExistsException(Long orderId) {
        super("A successful payment already exists for orderId: " + orderId);
    }
}
