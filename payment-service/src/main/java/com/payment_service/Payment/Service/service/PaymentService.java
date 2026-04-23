package com.payment_service.Payment.Service.service;



import com.payment_service.Payment.Service.dto.request.InitiatePaymentRequest;
import com.payment_service.Payment.Service.dto.request.PaymentVerificationRequest;
import com.payment_service.Payment.Service.dto.request.RefundRequest;
import com.payment_service.Payment.Service.dto.response.InitiatePaymentResponse;
import com.payment_service.Payment.Service.dto.response.PaymentResponse;

import java.util.List;

public interface PaymentService {

    /**
     * Creates a Razorpay order and persists a PENDING payment record.
     * Returns the Razorpay order ID and public key so the frontend
     * can open the Razorpay checkout modal.
     */
    InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request);

    /**
     * Verifies the HMAC-SHA256 signature from Razorpay, marks the payment
     * SUCCESS, and updates the order status to CONFIRMED via order-service.
     */
    PaymentResponse verifyAndCapturePayment(PaymentVerificationRequest request);

    /**
     * Issues a full or partial refund via Razorpay and marks the payment REFUNDED.
     * Also updates the order status to REFUNDED via order-service.
     */
    PaymentResponse refundPayment(Long paymentId, RefundRequest request);

    PaymentResponse getPaymentById(Long id);

    PaymentResponse getPaymentByOrderId(Long orderId);

    List<PaymentResponse> getPaymentsByUserId(Long userId);
}
