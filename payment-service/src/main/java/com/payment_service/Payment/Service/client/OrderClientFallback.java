package com.payment_service.Payment.Service.client;




import com.payment_service.Payment.Service.dto.request.UpdateOrderStatusRequest;
import com.payment_service.Payment.Service.dto.response.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderClientFallback implements OrderClient {

    @Override
    public OrderResponse getOrderById(Long id) {
        log.warn("OrderService unavailable — fallback getOrderById for id: {}", id);
        return null;  // service layer checks for null and throws PaymentException
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, UpdateOrderStatusRequest request) {
        log.error("OrderService unavailable — could not update order {} to status: {}",
                id, request.getStatus());
        return null;
    }
}