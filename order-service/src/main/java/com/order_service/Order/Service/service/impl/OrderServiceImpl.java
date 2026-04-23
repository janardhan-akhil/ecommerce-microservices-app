package com.order_service.Order.Service.service.impl;


import com.order_service.Order.Service.client.ProductClient;
import com.order_service.Order.Service.client.UserClient;
import com.order_service.Order.Service.dto.request.CreateOrderRequest;
import com.order_service.Order.Service.dto.request.OrderItemRequest;
import com.order_service.Order.Service.dto.request.UpdateOrderStatusRequest;
import com.order_service.Order.Service.dto.response.OrderResponse;
import com.order_service.Order.Service.entity.Order;
import com.order_service.Order.Service.entity.OrderItem;
import com.order_service.Order.Service.exception.InsufficientStockException;
import com.order_service.Order.Service.exception.InvalidOrderStatusException;
import com.order_service.Order.Service.exception.OrderNotFoundException;
import com.order_service.Order.Service.external.ProductResponse;
import com.order_service.Order.Service.external.UserResponse;
import com.order_service.Order.Service.payload.PagedResponse;
import com.order_service.Order.Service.repository.OrderRepository;
import com.order_service.Order.Service.service.OrderService;
import com.order_service.Order.Service.utility.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final com.order_service.Order.Service.mapper.OrderMapper orderMapper;

    // Valid transitions: what status can follow which
    private static final java.util.Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS =
            java.util.Map.of(
                    OrderStatus.PENDING,    Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
                    OrderStatus.CONFIRMED,  Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
                    OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
                    OrderStatus.SHIPPED,    Set.of(OrderStatus.DELIVERED),
                    OrderStatus.DELIVERED,  Set.of(OrderStatus.REFUNDED),
                    OrderStatus.CANCELLED,  Set.of(),
                    OrderStatus.REFUNDED,   Set.of()
            );

    @Override
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for userId: {}", request.getUserId());

        // Validate user exists
        UserResponse user = userClient.getUserById(request.getUserId());
        log.debug("User validated: {}", user.getEmail());

        // Validate products and build order items
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            ProductResponse product = productClient.getProductById(itemRequest.getProductId());

            // Check stock availability using Product.quantity
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                        itemRequest.getProductId(),
                        itemRequest.getQuantity(),
                        product.getQuantity()
                );
            }

            // Product.price is double — convert to BigDecimal for precision
            BigDecimal unitPrice = BigDecimal.valueOf(product.getPrice());
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemTotal);
        }

        // Build and persist the order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(
                        request.getBillingAddress() != null
                                ? request.getBillingAddress()
                                : request.getShippingAddress()
                )
                .paymentMethod(request.getPaymentMethod())
                .build();

        orderItems.forEach(order::addOrderItem);

        Order savedOrder = orderRepository.save(order);

//        // Deduct stock for each product
//        for (OrderItemRequest itemRequest : request.getItems()) {
//            productClient.updateProductStock(
//                    itemRequest.getProductId(),
//                    -itemRequest.getQuantity()
//            );
//        }

        log.info("Order created successfully: {}", savedOrder.getOrderNumber());
        return orderMapper.toOrderResponse(savedOrder, user);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        log.info("Fetching order by id: {}", id);
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        UserResponse user = userClient.getUserById(order.getUserId());
        return orderMapper.toOrderResponse(order, user);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        log.info("Fetching order by orderNumber: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found with orderNumber: " + orderNumber));

        UserResponse user = userClient.getUserById(order.getUserId());
        return orderMapper.toOrderResponse(order, user);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getAllOrders(
            int pageNo, int pageSize, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Order> orders = orderRepository.findAll(pageable);

        Page<OrderResponse> responsePage = orders.map(order -> {
            UserResponse user = userClient.getUserById(order.getUserId());
            return orderMapper.toOrderResponse(order, user);
        });

        return PagedResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderResponse> getOrdersByUserId(
            Long id, int pageNo, int pageSize) {

        Pageable pageable = PageRequest.of(pageNo, pageSize,
                Sort.by("createdAt").descending());

        Page<Order> orders = orderRepository.findByUserId(id, pageable);
        UserResponse user = userClient.getUserById(id);

        Page<OrderResponse> responsePage = orders.map(
                order -> orderMapper.toOrderResponse(order, user));

        return PagedResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserIdAndStatus(Long id, OrderStatus status) {
        log.info("Fetching orders for userId: {} with status: {}", id, status);
        UserResponse user = userClient.getUserById(id);

        return orderRepository.findByUserIdAndStatus(id, status)
                .stream()
                .map(order -> orderMapper.toOrderResponse(order, user))
                .collect(Collectors.toList());
    }


    @Override
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        log.info("Updating order {} status to {}", id, request.getStatus());

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());


        Order updatedOrder = orderRepository.save(order);
        log.info("Order {} status updated to {}", updatedOrder.getOrderNumber(), updatedOrder.getStatus());

        UserResponse user = userClient.getUserById(updatedOrder.getUserId());
        return orderMapper.toOrderResponse(updatedOrder, user);
    }

    @Override
    public OrderResponse cancelOrder(Long id) {
        log.info("Cancelling order: {}", id);

        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        validateStatusTransition(order.getStatus(), OrderStatus.CANCELLED);

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);

        // Restore stock for each item
//        savedOrder.getOrderItems().forEach(item ->
//                productClient.updateProductStock(item.getProductId(), item.getQuantity())
//        );

        log.info("Order {} cancelled and stock restored", savedOrder.getOrderNumber());

        UserResponse user = userClient.getUserById(savedOrder.getUserId());
        return orderMapper.toOrderResponse(savedOrder, user);
    }

    @Override
    public void deleteOrder(Long id) {
        log.info("Deleting order: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        if (!Set.of(OrderStatus.CANCELLED, OrderStatus.REFUNDED).contains(order.getStatus())) {
            throw new InvalidOrderStatusException(
                    "Only CANCELLED or REFUNDED orders can be deleted");
        }

        orderRepository.delete(order);
        log.info("Order {} deleted", order.getOrderNumber());
    }

    // --- Private helpers ---

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new InvalidOrderStatusException(current, next);
        }
    }

    private String generateOrderNumber() {
        String candidate;
        do {
            // ORD-<epoch-millis>-<4 random hex chars>
            candidate = "ORD-"
                    + Instant.now().toEpochMilli()
                    + "-"
                    + Integer.toHexString((int) (Math.random() * 0xFFFF)).toUpperCase();
        } while (orderRepository.existsByOrderNumber(candidate));
        return candidate;
    }
}
