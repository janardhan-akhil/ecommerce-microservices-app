package com.order_service.Order.Service.service;




import com.order_service.Order.Service.dto.request.CreateOrderRequest;
import com.order_service.Order.Service.dto.request.UpdateOrderStatusRequest;
import com.order_service.Order.Service.dto.response.OrderResponse;
import com.order_service.Order.Service.payload.PagedResponse;
import com.order_service.Order.Service.utility.OrderStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    OrderResponse getOrderByOrderNumber(String orderNumber);

    PagedResponse<OrderResponse> getAllOrders(int pageNo, int pageSize, String sortBy, String sortDir);

    PagedResponse<OrderResponse> getOrdersByUserId(Long id, int pageNo, int pageSize);

    List<OrderResponse> getOrdersByUserIdAndStatus(Long id, OrderStatus status);

    OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request);

    OrderResponse cancelOrder(Long id);

    void deleteOrder(Long id);
}
