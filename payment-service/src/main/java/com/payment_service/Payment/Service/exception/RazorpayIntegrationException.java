package com.payment_service.Payment.Service.exception;


public class RazorpayIntegrationException extends RuntimeException {
    public RazorpayIntegrationException(String message) {
        super(message);
    }
    public RazorpayIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
