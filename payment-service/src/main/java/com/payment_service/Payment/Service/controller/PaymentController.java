package com.payment_service.Payment.Service.controller;


import com.payment_service.Payment.Service.dto.request.InitiatePaymentRequest;
import com.payment_service.Payment.Service.dto.request.PaymentVerificationRequest;
import com.payment_service.Payment.Service.dto.request.RefundRequest;
import com.payment_service.Payment.Service.dto.response.ApiResponse;
import com.payment_service.Payment.Service.dto.response.InitiatePaymentResponse;
import com.payment_service.Payment.Service.dto.response.PaymentResponse;
import com.payment_service.Payment.Service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * POST /api/v1/payments/initiate
     *
     * Step 1 of the payment flow.
     * Creates a Razorpay order and returns the credentials needed for the
     * frontend to open the Razorpay checkout modal.
     *
     * Frontend usage:
     *   var rzp = new Razorpay({
     *       key:      response.data.keyId,
     *       amount:   response.data.amountInPaise,
     *       currency: response.data.currency,
     *       order_id: response.data.razorpayOrderId
     *   });
     *   rzp.open();
     */
    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request) {
        log.info("POST /api/v1/payments/initiate - orderId={}", request.getOrderId());
        InitiatePaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated successfully", response));
    }

    /**
     * POST /api/v1/payments/verify
     *
     * Step 2 of the payment flow.
     * The frontend calls this after the user completes payment in the Razorpay modal.
     * The three Razorpay fields are sent as-is from the checkout callback.
     *
     * On success: payment marked SUCCESS, order status updated to CONFIRMED.
     * On failure: payment marked FAILED, PaymentVerificationException thrown.
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody PaymentVerificationRequest request) {
        log.info("POST /api/v1/payments/verify - razorpayOrderId={}",
                request.getRazorpayOrderId());
        PaymentResponse response = paymentService.verifyAndCapturePayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", response));
    }

    /**
     * POST /api/v1/payments/{id}/refund
     *
     * Initiates a full or partial refund.
     * If RefundRequest.amount is null, the full payment amount is refunded.
     * On success: payment marked REFUNDED, order status updated to REFUNDED.
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request) {
        log.info("POST /api/v1/payments/{}/refund amount={}", id, request.getAmount());
        PaymentResponse response = paymentService.refundPayment(id, request);
        return ResponseEntity.ok(ApiResponse.success("Refund processed successfully", response));
    }

    /**
     * GET /api/v1/payments/{id}
     * Fetch a payment record by its internal ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long id) {
        log.info("GET /api/v1/payments/{}", id);
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentById(id)));
    }

    /**
     * GET /api/v1/payments/order/{orderId}
     * Fetch the payment associated with a specific order.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable Long orderId) {
        log.info("GET /api/v1/payments/order/{}", orderId);
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getPaymentByOrderId(orderId)));
    }

    /**
     * GET /api/v1/payments/user/{userId}
     * Fetch all payments for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByUser(
            @PathVariable Long userId) {
        log.info("GET /api/v1/payments/user/{}", userId);
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getPaymentsByUserId(userId)));
    }
}
