package com.cart_service.Cart.Service.service;


import com.cart_service.Cart.Service.dto.request.AddToCartRequest;
import com.cart_service.Cart.Service.dto.request.CheckoutRequest;
import com.cart_service.Cart.Service.dto.request.UpdateCartItemRequest;
import com.cart_service.Cart.Service.dto.response.CartResponse;
import com.cart_service.Cart.Service.external.OrderResponse;

public interface CartService {

    /** Add a product to the cart. If it already exists, increment the quantity. */
    CartResponse addToCart(Long userId, AddToCartRequest request);

    /** Get the full cart for a user. Throws CartNotFoundException if none exists. */
    CartResponse getCart(Long userId);

    /** Set the exact quantity for an existing cart item. */
    CartResponse updateItemQuantity(Long userId, Long productId, UpdateCartItemRequest request);

    /** Remove a single product line from the cart. */
    CartResponse removeItem(Long userId, Long productId);

    /** Delete the entire cart for the user. */
    void clearCart(Long userId);

    /** Convert the cart into an order via Order Service, then clear the cart. */
    OrderResponse checkout(Long userId, CheckoutRequest request);
}
