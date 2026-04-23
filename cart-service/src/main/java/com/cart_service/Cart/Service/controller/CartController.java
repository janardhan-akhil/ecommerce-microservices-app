package com.cart_service.Cart.Service.controller;


import com.cart_service.Cart.Service.dto.request.AddToCartRequest;
import com.cart_service.Cart.Service.dto.request.CheckoutRequest;
import com.cart_service.Cart.Service.dto.request.UpdateCartItemRequest;
import com.cart_service.Cart.Service.dto.response.CartResponse;
import com.cart_service.Cart.Service.external.OrderResponse;
import com.cart_service.Cart.Service.payload.ApiResponse;
import com.cart_service.Cart.Service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * POST /api/v1/cart/{userId}/items
     * Add a product to the cart.
     * If the product is already in the cart the quantities are merged.
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            @PathVariable Long userId,
            @Valid @RequestBody AddToCartRequest request) {

        log.info("POST /api/cart/{}/items - productId={}", userId, request.getProductId());
        CartResponse cart = cartService.addToCart(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", cart));
    }

    /**
     * GET /api/v1/cart/{userId}
     * Retrieve the full cart for a user including all line items and totals.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(@PathVariable Long userId) {
        log.info("GET /api/cart/{}", userId);
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * PUT /api/v1/cart/{userId}/items/{productId}
     * Set the exact quantity for an existing cart item.
     * Validates stock availability before updating.
     */
    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        log.info("PUT /api/cart/{}/items/{} - qty={}", userId, productId, request.getQuantity());
        CartResponse cart = cartService.updateItemQuantity(userId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", cart));
    }

    /**
     * DELETE /api/v1/cart/{userId}/items/{productId}
     * Remove a single product line from the cart.
     */
    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {

        log.info("DELETE /api/cart/{}/items/{}", userId, productId);
        CartResponse cart = cartService.removeItem(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    /**
     * DELETE /api/v1/cart/{userId}
     * Clear the entire cart for a user.
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> clearCart(@PathVariable Long userId) {
        log.info("DELETE /api/cart/{}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }

    /**
     * POST /api/v1/cart/{userId}/checkout
     * Convert the cart into an order via Order Service.
     * On success the cart is automatically cleared.
     */
    @PostMapping("/{userId}/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(
            @PathVariable Long userId,
            @Valid @RequestBody CheckoutRequest request) {

        log.info("POST /api/cart/{}/checkout", userId);
        OrderResponse order = cartService.checkout(userId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", order));
    }



}
