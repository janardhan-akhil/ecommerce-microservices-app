package com.payment_service.Payment.Service.dto.request;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RefundRequest {

    /**
     * Amount to refund in INR.
     * If null, the full payment amount is refunded.
     */
    @DecimalMin(value = "1.0", message = "Refund amount must be at least ₹1")
    private BigDecimal amount;

    private String reason;  // optional — displayed in Razorpay dashboard
}
