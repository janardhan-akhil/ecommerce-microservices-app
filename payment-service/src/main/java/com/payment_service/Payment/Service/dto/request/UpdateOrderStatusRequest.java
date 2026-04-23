package com.payment_service.Payment.Service.dto.request;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UpdateOrderStatusRequest {
    private String status;  // matches OrderStatus enum in order-service
    private String notes;
}
