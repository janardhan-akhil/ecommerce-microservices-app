package com.payment_service.Payment.Service.utility;


public enum PaymentStatus {
    PENDING,    // Razorpay order created, awaiting user action
    SUCCESS,    // Signature verified, money collected
    FAILED,     // Payment attempt failed or signature mismatch
    REFUNDED    // Full or partial refund issued
}
