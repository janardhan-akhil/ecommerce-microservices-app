package com.payment_service.Payment.Service.service;

import com.payment_service.Payment.Service.client.OrderClient;
import com.payment_service.Payment.Service.client.UserClient;
import com.payment_service.Payment.Service.config.PaymentMapper;
import com.payment_service.Payment.Service.dto.request.InitiatePaymentRequest;
import com.payment_service.Payment.Service.dto.request.PaymentVerificationRequest;
import com.payment_service.Payment.Service.dto.request.RefundRequest;
import com.payment_service.Payment.Service.dto.response.OrderResponse;
import com.payment_service.Payment.Service.dto.response.PaymentResponse;
import com.payment_service.Payment.Service.dto.response.UserResponse;
import com.payment_service.Payment.Service.entity.Payment;
import com.payment_service.Payment.Service.exception.*;
import com.payment_service.Payment.Service.repository.PaymentRepository;
import com.payment_service.Payment.Service.service.impl.PaymentServiceImpl;
import com.payment_service.Payment.Service.utility.PaymentStatus;
import com.razorpay.RazorpayClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private RazorpayClient razorpayClient;
    @Mock private OrderClient orderClient;
    @Mock private UserClient userClient;
    @Mock private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment mockPayment;
    private PaymentResponse mockPaymentResponse;
    private OrderResponse mockOrder;
    private UserResponse mockUser;

    private static final String TEST_KEY_SECRET = "testSecretKey1234567890abcdef123456";
    private static final String TEST_KEY_ID     = "rzp_test_testKeyId";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(paymentService, "razorpayKeyId",     TEST_KEY_ID);
        ReflectionTestUtils.setField(paymentService, "razorpayKeySecret", TEST_KEY_SECRET);
        ReflectionTestUtils.setField(paymentService, "defaultCurrency",   "INR");

        mockPayment = Payment.builder()
                .id(1L).orderId(10L).userId(5L)
                .razorpayOrderId("order_test123")
                .razorpayPaymentId(null)
                .amount(new BigDecimal("999.00"))
                .currency("INR")
                .status(PaymentStatus.PENDING)
                .build();

        mockPaymentResponse = PaymentResponse.builder()
                .id(1L).orderId(10L).userId(5L)
                .razorpayOrderId("order_test123")
                .amount(new BigDecimal("999.00"))
                .status(PaymentStatus.PENDING)
                .build();

        mockOrder = OrderResponse.builder()
                .id(10L).orderNumber("ORD-001").userId(5L)
                .status("PENDING").totalAmount(new BigDecimal("999.00"))
                .build();

        mockUser = UserResponse.builder()
                .id(5L).name("John Doe").email("john@test.com").role("CUSTOMER")
                .build();
    }

    // ── initiatePayment ───────────────────────────────────────────────

    @Test
    @DisplayName("initiatePayment - throws when a successful payment already exists")
    void initiatePayment_alreadyExists_throws() {
        when(paymentRepository.existsByOrderIdAndStatus(10L, PaymentStatus.SUCCESS))
                .thenReturn(true);

        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(10L).userId(5L).amount(new BigDecimal("999.00")).build();

        assertThatThrownBy(() -> paymentService.initiatePayment(req))
                .isInstanceOf(PaymentAlreadyExistsException.class)
                .hasMessageContaining("10");
    }

    @Test
    @DisplayName("initiatePayment - throws when order-service returns null (unavailable)")
    void initiatePayment_orderServiceDown_throws() {
        when(paymentRepository.existsByOrderIdAndStatus(any(), any())).thenReturn(false);
        when(orderClient.getOrderById(10L)).thenReturn(null);

        InitiatePaymentRequest req = InitiatePaymentRequest.builder()
                .orderId(10L).userId(5L).amount(new BigDecimal("999.00")).build();

        assertThatThrownBy(() -> paymentService.initiatePayment(req))
                .isInstanceOf(RazorpayIntegrationException.class)
                .hasMessageContaining("Order service unavailable");
    }

    // ── verifyAndCapturePayment ───────────────────────────────────────

    @Test
    @DisplayName("verifyAndCapturePayment - throws when payment record not found")
    void verify_paymentNotFound_throws() {
        when(paymentRepository.findByRazorpayOrderId("order_xyz"))
                .thenReturn(Optional.empty());

        PaymentVerificationRequest req = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_xyz")
                .razorpayPaymentId("pay_abc")
                .razorpaySignature("invalidsig")
                .build();

        assertThatThrownBy(() -> paymentService.verifyAndCapturePayment(req))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    @DisplayName("verifyAndCapturePayment - throws and marks FAILED on signature mismatch")
    void verify_signatureMismatch_throwsAndMarksFailed() {
        when(paymentRepository.findByRazorpayOrderId("order_test123"))
                .thenReturn(Optional.of(mockPayment));
        when(paymentRepository.save(any())).thenReturn(mockPayment);

        PaymentVerificationRequest req = PaymentVerificationRequest.builder()
                .razorpayOrderId("order_test123")
                .razorpayPaymentId("pay_abc123")
                .razorpaySignature("wrong_signature_that_wont_match")
                .build();

        assertThatThrownBy(() -> paymentService.verifyAndCapturePayment(req))
                .isInstanceOf(PaymentVerificationException.class)
                .hasMessageContaining("signature verification failed");

        assertThat(mockPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        verify(paymentRepository).save(mockPayment);
    }

    // ── refundPayment ─────────────────────────────────────────────────

    @Test
    @DisplayName("refundPayment - throws when payment not found")
    void refund_notFound_throws() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refundPayment(99L,
                RefundRequest.builder().build()))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("refundPayment - throws when payment is not in SUCCESS state")
    void refund_notSuccessful_throws() {
        mockPayment.setStatus(PaymentStatus.PENDING);
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L,
                RefundRequest.builder().amount(new BigDecimal("100")).build()))
                .isInstanceOf(RefundException.class)
                .hasMessageContaining("Only successful payments");
    }

    @Test
    @DisplayName("refundPayment - throws when refund amount exceeds original payment")
    void refund_excessAmount_throws() {
        mockPayment.setStatus(PaymentStatus.SUCCESS);
        mockPayment.setRazorpayPaymentId("pay_abc123");
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));

        assertThatThrownBy(() -> paymentService.refundPayment(1L,
                RefundRequest.builder().amount(new BigDecimal("9999.00")).build()))
                .isInstanceOf(RefundException.class)
                .hasMessageContaining("exceeds original payment");
    }

    // ── getPaymentById ────────────────────────────────────────────────

    @Test
    @DisplayName("getPaymentById - returns mapped response when found")
    void getPaymentById_found() {
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(mockPayment));
        when(paymentMapper.toPaymentResponse(mockPayment)).thenReturn(mockPaymentResponse);

        PaymentResponse result = paymentService.getPaymentById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getPaymentById - throws when not found")
    void getPaymentById_notFound_throws() {
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPaymentById(999L))
                .isInstanceOf(PaymentNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ── getPaymentsByUserId ───────────────────────────────────────────

    @Test
    @DisplayName("getPaymentsByUserId - returns all payments for the user")
    void getPaymentsByUserId_returnsList() {
        when(paymentRepository.findByUserId(5L)).thenReturn(List.of(mockPayment));
        when(paymentMapper.toPaymentResponse(mockPayment)).thenReturn(mockPaymentResponse);

        List<PaymentResponse> results = paymentService.getPaymentsByUserId(5L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(5L);
    }
}
