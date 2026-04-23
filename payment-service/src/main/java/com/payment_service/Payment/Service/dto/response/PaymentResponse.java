package com.payment_service.Payment.Service.dto.response;




import com.payment_service.Payment.Service.utility.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;

    // Razorpay identifiers
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpayRefundId;

    // Amount
    private BigDecimal amount;
    private BigDecimal refundAmount;
    private String currency;

    // Status
    private PaymentStatus status;
    private String failureReason;
    private String notes;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
}
