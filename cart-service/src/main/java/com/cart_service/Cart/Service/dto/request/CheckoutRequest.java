package com.cart_service.Cart.Service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    private String billingAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

}
