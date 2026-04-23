package com.order_service.Order.Service.controller;


import com.order_service.Order.Service.dto.request.CreateOrderRequest;
import com.order_service.Order.Service.dto.request.UpdateOrderStatusRequest;
import com.order_service.Order.Service.dto.response.OrderResponse;
import com.order_service.Order.Service.payload.ApiResponse;
import com.order_service.Order.Service.payload.PagedResponse;
import com.order_service.Order.Service.service.OrderService;
import com.order_service.Order.Service.utility.OrderStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/v1/orders
     * Create a new order. Validates user, checks product stock,
     * persists the order, and deducts stock from product service.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("REST POST /api/orders - creating order for userId: {}", request.getUserId());
        OrderResponse order = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", order));
    }

    /**
     * GET /api/v1/orders/{id}
     * Fetch a single order by its internal numeric ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        log.info("REST GET /api/orders/{}", id);
        OrderResponse order = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * GET /api/v1/orders/number/{orderNumber}
     * Fetch an order by its human-readable order number (e.g. ORD-17XXXXX-A3B2).
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderByNumber(
            @PathVariable String orderNumber) {
        log.info("REST GET /api/orders/number/{}", orderNumber);
        OrderResponse order = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(ApiResponse.success(order));
    }

    /**
     * GET /api/v1/orders
     * Paginated list of all orders (admin use). Supports sorting.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0")  int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("REST GET /api/orders - page={}, size={}", pageNo, pageSize);
        PagedResponse<OrderResponse> orders =
                orderService.getAllOrders(pageNo, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * GET /api/v1/orders/user/{userId}
     * Paginated order history for a specific user.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<PagedResponse<OrderResponse>>> getOrdersByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int pageNo,
            @RequestParam(defaultValue = "10") int pageSize) {

        log.info("REST GET /api/orders/user/{}", userId);
        PagedResponse<OrderResponse> orders =
                orderService.getOrdersByUserId(userId, pageNo, pageSize);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * GET /api/v1/orders/user/{userId}/status?status=PENDING
     * Filter a user's orders by status.
     */
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByUserAndStatus(
            @PathVariable Long userId,
            @RequestParam OrderStatus status) {

        log.info("REST GET /api/orders/user/{}/status?status={}", userId, status);
        List<OrderResponse> orders = orderService.getOrdersByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * PATCH /api/v1/orders/{id}/status
     * Update the status of an order. Enforces valid state-machine transitions.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        log.info("REST PATCH /api/orders/{}/status -> {}", id, request.getStatus());
        OrderResponse order = orderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", order));
    }

    /**
     * POST /api/v1/orders/{id}/cancel
     * Cancel an order and restore product stock.
     * Only PENDING, CONFIRMED, and PROCESSING orders can be cancelled.
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        log.info("REST POST /api/orders/{}/cancel", id);
        OrderResponse order = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", order));
    }

    /**
     * DELETE /api/v1/orders/{id}
     * Hard-delete an order record. Only allowed for CANCELLED or REFUNDED orders.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable Long id) {
        log.info("REST DELETE /api/orders/{}", id);
        orderService.deleteOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order deleted successfully", null));
    }
}
