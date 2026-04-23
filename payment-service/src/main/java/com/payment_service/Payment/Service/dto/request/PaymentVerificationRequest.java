package com.payment_service.Payment.Service.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentVerificationRequest {

    /**
     * razorpay_order_id — returned by Razorpay when the order was created.
     * Also returned by the Razorpay checkout callback.
     */
    @NotBlank(message = "razorpayOrderId is required")
    private String razorpayOrderId;

    /**
     * razorpay_payment_id — provided by Razorpay after the user completes payment.
     * Sent from frontend to backend for server-side verification.
     */
    @NotBlank(message = "razorpayPaymentId is required")
    private String razorpayPaymentId;

    /**
     * razorpay_signature — HMAC-SHA256 of "<razorpay_order_id>|<razorpay_payment_id>"
     * using your key_secret. Must be verified server-side before marking payment as SUCCESS.
     */
    @NotBlank(message = "razorpaySignature is required")
    private String razorpaySignature;
}
