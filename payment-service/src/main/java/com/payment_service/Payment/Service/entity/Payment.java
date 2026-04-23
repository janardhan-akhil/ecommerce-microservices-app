package com.payment_service.Payment.Service.entity;



import com.payment_service.Payment.Service.utility.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;                       // our internal order-service order id

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // ── Razorpay IDs ─────────────────────────────────────────────────
    @Column(name = "razorpay_order_id", unique = true)
    private String razorpayOrderId;             // rpay_xxxx  — created on initiate

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;           // pay_xxxx   — provided by frontend after payment

    @Column(name = "razorpay_signature")
    private String razorpaySignature;           // HMAC-SHA256 signature verified on verify

    @Column(name = "razorpay_refund_id")
    private String razorpayRefundId;            // rfnd_xxxx  — set on refund

    // ── Amounts ───────────────────────────────────────────────────────
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;                  // in INR (display value — Razorpay uses paise)

    @Column(name = "refund_amount", precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "INR";

    // ── Status ────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // ── Timestamps ────────────────────────────────────────────────────
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;
}
