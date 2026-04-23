package com.cart_service.Cart.Service.mapper;


import com.cart_service.Cart.Service.dto.response.CartItemResponse;
import com.cart_service.Cart.Service.dto.response.CartResponse;
import com.cart_service.Cart.Service.entity.Cart;
import com.cart_service.Cart.Service.entity.CartItem;
import com.cart_service.Cart.Service.external.UserResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartResponse toCartResponse(Cart cart, UserResponse user) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());

        return CartResponse.builder()
                .userId(cart.getUserId())
                .customerName(user != null ? user.getName() : null)
                .items(itemResponses)
                .totalItems(cart.getItems().size())
                .totalQuantity(cart.totalQuantity())
                .totalAmount(cart.getTotalAmount())
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    public CartResponse toCartResponse(Cart cart) {
        return toCartResponse(cart, null);
    }

    public CartItemResponse toCartItemResponse(CartItem item) {
        return CartItemResponse.builder()
                .productId(item.getProductId())
                .productName(item.getProductName())
                .category(item.getCategory())
                .brand(item.getBrand())
                .imageName(item.getImageName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .totalPrice(item.getTotalPrice())
                .build();
    }
}
