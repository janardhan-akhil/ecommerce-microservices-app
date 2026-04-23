package com.payment_service.Payment.Service.service.impl;


import com.payment_service.Payment.Service.client.OrderClient;
import com.payment_service.Payment.Service.client.UserClient;
import com.payment_service.Payment.Service.config.PaymentMapper;
import com.payment_service.Payment.Service.dto.request.InitiatePaymentRequest;
import com.payment_service.Payment.Service.dto.request.PaymentVerificationRequest;
import com.payment_service.Payment.Service.dto.request.RefundRequest;
import com.payment_service.Payment.Service.dto.request.UpdateOrderStatusRequest;
import com.payment_service.Payment.Service.dto.response.InitiatePaymentResponse;
import com.payment_service.Payment.Service.dto.response.OrderResponse;
import com.payment_service.Payment.Service.dto.response.PaymentResponse;
import com.payment_service.Payment.Service.entity.Payment;
import com.payment_service.Payment.Service.exception.*;
import com.payment_service.Payment.Service.repository.PaymentRepository;
import com.payment_service.Payment.Service.service.PaymentService;
import com.payment_service.Payment.Service.utility.PaymentStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final PaymentMapper paymentMapper;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${razorpay.currency:INR}")
    private String defaultCurrency;

    // ── Initiate ──────────────────────────────────────────────────────

    @Override
    public InitiatePaymentResponse initiatePayment(InitiatePaymentRequest request) {
        log.info("Initiating payment for orderId={} userId={} amount={}",
                request.getOrderId(), request.getUserId(), request.getAmount());

        // Guard: don't create a second successful payment for the same order
        if (paymentRepository.existsByOrderIdAndStatus(
                request.getOrderId(), PaymentStatus.SUCCESS)) {
            throw new PaymentAlreadyExistsException(request.getOrderId());
        }

        // Validate order exists via order-service
        OrderResponse order = orderClient.getOrderById(request.getOrderId());
        if (order == null) {
            throw new RazorpayIntegrationException(
                    "Order service unavailable or order not found: " + request.getOrderId());
        }

        // Validate user exists via user-service
        userClient.getUserById(request.getUserId());

        String currency = request.getCurrency() != null
                ? request.getCurrency() : defaultCurrency;

        // Razorpay requires amount in paise (₹1 = 100 paise)
        long amountInPaise = request.getAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Create Razorpay order
        String razorpayOrderId = createRazorpayOrder(
                amountInPaise, currency, request.getOrderId(), request.getNotes());

        // Persist PENDING payment record
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .razorpayOrderId(razorpayOrderId)
                .amount(request.getAmount())
                .currency(currency)
                .status(PaymentStatus.PENDING)
                .notes(request.getNotes())
                .build();

        Payment saved = paymentRepository.save(payment);
        log.info("Payment record created: id={} razorpayOrderId={}", saved.getId(), razorpayOrderId);

        return InitiatePaymentResponse.builder()
                .paymentId(saved.getId())
                .razorpayOrderId(razorpayOrderId)
                .amount(request.getAmount())
                .amountInPaise(amountInPaise)
                .currency(currency)
                .keyId(razorpayKeyId)    // public key — safe to send to frontend
                .status("PENDING")
                .build();
    }

    // ── Verify & capture ──────────────────────────────────────────────

    @Override
    public PaymentResponse verifyAndCapturePayment(PaymentVerificationRequest request) {
        log.info("Verifying payment: razorpayOrderId={} razorpayPaymentId={}",
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());

        // Load the payment record by Razorpay order ID
        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for razorpayOrderId: "
                                + request.getRazorpayOrderId()));

        // ── HMAC-SHA256 signature verification ────────────────────────
        // Razorpay signs: "<razorpay_order_id>|<razorpay_payment_id>"
        // We verify this server-side before trusting the payment.
        String payload   = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
        String generated = generateHmacSha256(payload, razorpayKeySecret);

        if (!generated.equals(request.getRazorpaySignature())) {
            log.warn("Signature mismatch for razorpayOrderId={}", request.getRazorpayOrderId());

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);

            throw new PaymentVerificationException(
                    "Payment signature verification failed. Payment marked as FAILED.");
        }

        // Signature valid — mark as SUCCESS
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        log.info("Payment verified and captured: id={} razorpayPaymentId={}",
                saved.getId(), request.getRazorpayPaymentId());

        // ── Update order status to CONFIRMED via order-service ────────
        updateOrderStatus(payment.getOrderId(), "CONFIRMED",
                "Payment captured: " + request.getRazorpayPaymentId());

        return paymentMapper.toPaymentResponse(saved);
    }

    // ── Refund ────────────────────────────────────────────────────────

    @Override
    public PaymentResponse refundPayment(Long paymentId, RefundRequest request) {
        log.info("Processing refund for paymentId={}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RefundException(
                    "Only successful payments can be refunded. Current status: "
                            + payment.getStatus());
        }

        if (payment.getRazorpayPaymentId() == null) {
            throw new RefundException("Razorpay payment ID not found on payment record.");
        }

        // Determine refund amount — null means full refund
        BigDecimal refundAmount = request.getAmount() != null
                ? request.getAmount()
                : payment.getAmount();

        if (refundAmount.compareTo(payment.getAmount()) > 0) {
            throw new RefundException(
                    "Refund amount ₹" + refundAmount
                            + " exceeds original payment amount ₹" + payment.getAmount());
        }

        long refundInPaise = refundAmount
                .multiply(BigDecimal.valueOf(100))
                .longValue();

        // Call Razorpay Refunds API
        String razorpayRefundId = createRazorpayRefund(
                payment.getRazorpayPaymentId(), refundInPaise, request.getReason());

        payment.setRazorpayRefundId(razorpayRefundId);
        payment.setRefundAmount(refundAmount);
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundedAt(LocalDateTime.now());

        Payment saved = paymentRepository.save(payment);
        log.info("Refund processed: paymentId={} razorpayRefundId={} amount={}",
                paymentId, razorpayRefundId, refundAmount);

        // Update order status to REFUNDED
        updateOrderStatus(payment.getOrderId(), "REFUNDED",
                "Refund issued: " + razorpayRefundId);

        return paymentMapper.toPaymentResponse(saved);
    }

    // ── Queries ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        return paymentMapper.toPaymentResponse(
                paymentRepository.findById(id)
                        .orElseThrow(() -> new PaymentNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        return paymentMapper.toPaymentResponse(
                paymentRepository.findByOrderId(orderId)
                        .orElseThrow(() -> new PaymentNotFoundException(
                                "Payment not found for orderId: " + orderId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toPaymentResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ───────────────────────────────────────────────

    /**
     * Creates a Razorpay order and returns the Razorpay order ID (e.g. order_xxxx).
     * Amount must be in the smallest currency unit (paise for INR).
     */
    private String createRazorpayOrder(long amountInPaise, String currency,
                                       Long orderId, String notes) {
        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount",   amountInPaise);
            orderRequest.put("currency", currency);
            orderRequest.put("receipt",  "order_" + orderId);  // must be ≤40 chars

            if (notes != null && !notes.isBlank()) {
                JSONObject notesObj = new JSONObject();
                notesObj.put("description", notes);
                orderRequest.put("notes", notesObj);
            }

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            log.debug("Razorpay order created: {}", razorpayOrderId);
            return razorpayOrderId;

        } catch (RazorpayException ex) {
            log.error("Failed to create Razorpay order: {}", ex.getMessage());
            throw new RazorpayIntegrationException(
                    "Failed to create payment order: " + ex.getMessage(), ex);
        }
    }

    /**
     * Issues a refund via Razorpay and returns the refund ID (e.g. rfnd_xxxx).
     * Amount must be in paise.
     */
    private String createRazorpayRefund(String razorpayPaymentId,
                                        long amountInPaise, String reason) {
        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", amountInPaise);
            if (reason != null && !reason.isBlank()) {
                refundRequest.put("notes", new JSONObject().put("reason", reason));
            }

            Refund refund = razorpayClient.payments.refund(razorpayPaymentId, refundRequest);
            String refundId = refund.get("id");

            log.debug("Razorpay refund created: {}", refundId);
            return refundId;

        } catch (RazorpayException ex) {
            log.error("Failed to create Razorpay refund: {}", ex.getMessage());
            throw new RefundException(
                    "Failed to process refund: " + ex.getMessage(), ex);
        }
    }

    /**
     * Generates HMAC-SHA256 hex digest.
     *
     * Razorpay signature verification:
     *   generated_signature = HMAC_SHA256(
     *       key   = razorpay_key_secret,
     *       data  = razorpay_order_id + "|" + razorpay_payment_id
     *   )
     * If generated_signature == razorpay_signature → payment is genuine.
     */
    private String generateHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new PaymentVerificationException(
                    "Failed to generate payment signature: " + ex.getMessage());
        }
    }

    /**
     * Calls order-service to update the order status.
     * Errors are logged but do not roll back the payment — the payment
     * record is the source of truth; the order can be reconciled later.
     */
    private void updateOrderStatus(Long orderId, String status, String notes) {
        try {
            UpdateOrderStatusRequest req = UpdateOrderStatusRequest.builder()
                    .status(status).notes(notes).build();
            orderClient.updateOrderStatus(orderId, req);
            log.info("Order {} updated to status: {}", orderId, status);
        } catch (Exception ex) {
            log.error("Failed to update order {} status to {}: {}",
                    orderId, status, ex.getMessage());
            // Intentionally not re-throwing — payment is captured, order sync is best-effort
        }
    }
}
