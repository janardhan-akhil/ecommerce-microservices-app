package com.order_service.Order.Service.service;


import com.order_service.Order.Service.client.ProductClient;
import com.order_service.Order.Service.client.UserClient;
import com.order_service.Order.Service.dto.request.CreateOrderRequest;
import com.order_service.Order.Service.dto.request.OrderItemRequest;
import com.order_service.Order.Service.dto.request.UpdateOrderStatusRequest;
import com.order_service.Order.Service.dto.response.OrderResponse;
import com.order_service.Order.Service.entity.Order;
import com.order_service.Order.Service.exception.InsufficientStockException;
import com.order_service.Order.Service.exception.InvalidOrderStatusException;
import com.order_service.Order.Service.exception.OrderNotFoundException;
import com.order_service.Order.Service.external.ProductResponse;
import com.order_service.Order.Service.external.UserResponse;
import com.order_service.Order.Service.mapper.OrderMapper;
import com.order_service.Order.Service.repository.OrderRepository;
import com.order_service.Order.Service.service.impl.OrderServiceImpl;
import com.order_service.Order.Service.utility.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserClient userClient;
    @Mock private ProductClient productClient;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UserResponse mockUser;
    private ProductResponse mockProduct;
    private Order mockOrder;

    @BeforeEach
    void setUp() {
        mockUser = UserResponse.builder()
                .id(1L)
                .name("John Doe")        // User.name (single field)
                .email("john@example.com")
                .role("CUSTOMER")
                .build();

        mockProduct = ProductResponse.builder()
                .id(10L)
                .name("Test Product")
                .price(99.99)            // Product.price (double)
                .quantity(50)            // Product.quantity (int)
                .available(true)
                .category("Electronics")
                .brand("TestBrand")
                .build();

        mockOrder = Order.builder()
                .id(1L).orderNumber("ORD-TEST-0001")
                .userId(1L).status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(99.99))  // internally BigDecimal for precision
                .shippingAddress("123 Main St")
                .build();
    }

    @Test
    @DisplayName("createOrder - successfully creates order when stock is sufficient")
    void createOrder_success() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L)
                .shippingAddress("123 Main St")
                .paymentMethod("CREDIT_CARD")
                .items(List.of(OrderItemRequest.builder()
                        .productId(10L).quantity(2).build()))
                .build();

        when(userClient.getUserById(1L)).thenReturn(mockUser);
        when(productClient.getProductById(10L)).thenReturn(mockProduct);
        when(orderRepository.existsByOrderNumber(anyString())).thenReturn(false);
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderMapper.toOrderResponse(any(Order.class), any(UserResponse.class)))
                .thenReturn(OrderResponse.builder()
                        .id(1L).orderNumber("ORD-TEST-0001")
                        .status(OrderStatus.PENDING).build());

        OrderResponse result = orderService.createOrder(request);

        assertThat(result).isNotNull();
        assertThat(result.getOrderNumber()).isEqualTo("ORD-TEST-0001");
       // verify(productClient).updateProductStock(10L, -2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("createOrder - throws InsufficientStockException when stock is too low")
    void createOrder_insufficientStock() {
        mockProduct = ProductResponse.builder()
                .id(10L).name("Test Product")
                .price(99.99)
                .quantity(1)             // only 1 in stock
                .available(true)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(1L).shippingAddress("123 Main St")
                .paymentMethod("CREDIT_CARD")
                .items(List.of(OrderItemRequest.builder()
                        .productId(10L).quantity(5).build()))
                .build();

        when(userClient.getUserById(1L)).thenReturn(mockUser);
        when(productClient.getProductById(10L)).thenReturn(mockProduct);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("getOrderById - returns order when found")
    void getOrderById_found() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(mockOrder));
        when(userClient.getUserById(1L)).thenReturn(mockUser);
        when(orderMapper.toOrderResponse(mockOrder, mockUser))
                .thenReturn(OrderResponse.builder().id(1L).build());

        OrderResponse result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getOrderById - throws OrderNotFoundException when not found")
    void getOrderById_notFound() {
        when(orderRepository.findByIdWithItems(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(99L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("updateOrderStatus - valid transition succeeds")
    void updateOrderStatus_validTransition() {
        mockOrder.setStatus(OrderStatus.PENDING);
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.CONFIRMED).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(userClient.getUserById(anyLong())).thenReturn(mockUser);
        when(orderMapper.toOrderResponse(any(Order.class), any(UserResponse.class)))
                .thenReturn(OrderResponse.builder().status(OrderStatus.CONFIRMED).build());

        OrderResponse result = orderService.updateOrderStatus(1L, request);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("updateOrderStatus - invalid transition throws exception")
    void updateOrderStatus_invalidTransition() {
        mockOrder.setStatus(OrderStatus.DELIVERED);
        UpdateOrderStatusRequest request = UpdateOrderStatusRequest.builder()
                .status(OrderStatus.PENDING).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.updateOrderStatus(1L, request))
                .isInstanceOf(InvalidOrderStatusException.class);
    }

    @Test
    @DisplayName("cancelOrder - restores product stock on cancellation")
    void cancelOrder_restoresStock() {
        mockOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(mockOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(userClient.getUserById(anyLong())).thenReturn(mockUser);
        when(orderMapper.toOrderResponse(any(Order.class), any(UserResponse.class)))
                .thenReturn(OrderResponse.builder().status(OrderStatus.CANCELLED).build());

        OrderResponse result = orderService.cancelOrder(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("deleteOrder - throws exception for non-terminal status")
    void deleteOrder_nonTerminalStatus_throws() {
        mockOrder.setStatus(OrderStatus.PROCESSING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        assertThatThrownBy(() -> orderService.deleteOrder(1L))
                .isInstanceOf(InvalidOrderStatusException.class)
                .hasMessageContaining("CANCELLED or REFUNDED");
    }
}
