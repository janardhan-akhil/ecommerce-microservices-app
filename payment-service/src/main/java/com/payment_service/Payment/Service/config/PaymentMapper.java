package com.payment_service.Payment.Service.config;


import com.payment_service.Payment.Service.dto.response.PaymentResponse;
import com.payment_service.Payment.Service.entity.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .razorpayOrderId(payment.getRazorpayOrderId())
                .razorpayPaymentId(payment.getRazorpayPaymentId())
                .razorpayRefundId(payment.getRazorpayRefundId())
                .amount(payment.getAmount())
                .refundAmount(payment.getRefundAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .failureReason(payment.getFailureReason())
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .paidAt(payment.getPaidAt())
                .refundedAt(payment.getRefundedAt())
                .build();
    }
}
