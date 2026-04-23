package com.payment_service.Payment.Service.client;



import com.payment_service.Payment.Service.dto.request.UpdateOrderStatusRequest;
import com.payment_service.Payment.Service.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {

    @GetMapping("/api/v1/orders/{id}")
    OrderResponse getOrderById(@PathVariable("id") Long id);

    @PatchMapping("/api/v1/orders/{id}/status")
    OrderResponse updateOrderStatus(@PathVariable("id") Long id,
                                    @RequestBody UpdateOrderStatusRequest request);


}