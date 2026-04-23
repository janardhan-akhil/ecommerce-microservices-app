package com.payment_service.Payment.Service.dto.response;


import lombok.*;

import java.math.BigDecimal;

/**
 * Returned to the frontend after a payment is initiated.
 *
 * The frontend uses razorpayOrderId, amount, currency, and keyId
 * to open the Razorpay checkout modal:
 *
 *   var options = {
 *       key: response.keyId,
 *       amount: response.amountInPaise,
 *       currency: response.currency,
 *       order_id: response.razorpayOrderId,
 *       ...
 *   };
 *   var rzp = new Razorpay(options);
 *   rzp.open();
 */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InitiatePaymentResponse {

    private Long paymentId;             // internal payment record id
    private String razorpayOrderId;     // rpay_xxxx — passed to Razorpay checkout

    private BigDecimal amount;          // INR display amount
    private Long amountInPaise;         // amount × 100 — used directly by Razorpay
    private String currency;

    private String keyId;               // Razorpay public key — safe to send to frontend

    private String status;              // always "PENDING" at this point
}
