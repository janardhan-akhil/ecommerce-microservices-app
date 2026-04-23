package com.cart_service.Cart.Service.service.impl;


import com.cart_service.Cart.Service.client.OrderClient;
import com.cart_service.Cart.Service.client.ProductClient;
import com.cart_service.Cart.Service.client.UserClient;
import com.cart_service.Cart.Service.config.RedisConfig;
import com.cart_service.Cart.Service.dto.request.*;
import com.cart_service.Cart.Service.dto.response.CartResponse;
import com.cart_service.Cart.Service.entity.Cart;
import com.cart_service.Cart.Service.entity.CartItem;
import com.cart_service.Cart.Service.exception.CartEmptyException;
import com.cart_service.Cart.Service.exception.CartNotFoundException;
import com.cart_service.Cart.Service.exception.CheckoutFailedException;
import com.cart_service.Cart.Service.exception.ProductNotAvailableException;
import com.cart_service.Cart.Service.external.OrderResponse;
import com.cart_service.Cart.Service.external.ProductResponse;
import com.cart_service.Cart.Service.external.UserResponse;
import com.cart_service.Cart.Service.mapper.CartMapper;
import com.cart_service.Cart.Service.repository.CartRepository;
import com.cart_service.Cart.Service.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final CartMapper cartMapper;
    private final RedisConfig redisConfig;

    // ── Add to cart ────────────────────────────────────────────────

    @Override
    public CartResponse addToCart(Long userId, AddToCartRequest request) {
        log.info("addToCart: userId={}, productId={}, qty={}",
                userId, request.getProductId(), request.getQuantity());

        // Validate user exists
        UserResponse user = userClient.getUserById(userId);

        // Validate product availability
        ProductResponse product = productClient.getProductById(request.getProductId());
        validateProduct(product, request.getProductId(), request.getQuantity());

        // Load or create cart
        Cart cart = cartRepository.findById(userId)
                .orElseGet(() -> createNewCart(userId));

        // Merge with existing item or add new line
        Optional<CartItem> existing = cart.findItem(request.getProductId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + request.getQuantity();

            // Re-check stock for the combined quantity
            if (product.getQuantity() < newQty) {
                throw new ProductNotAvailableException(
                        request.getProductId(), newQty, product.getQuantity());
            }
            item.setQuantity(newQty);
            item.recalculate();
            log.debug("Incremented existing item productId={} to qty={}", request.getProductId(), newQty);
        } else {
            CartItem newItem = CartItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .category(product.getCategory())
                    .brand(product.getBrand())
                    .imageName(product.getImageName())
                    .unitPrice(product.getPrice())        // Product.price (double)
                    .quantity(request.getQuantity())
                    .totalPrice(product.getPrice() * request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
            log.debug("Added new item productId={}", request.getProductId());
        }

        cart.recalculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTtl(redisConfig.getCartTtlSeconds());

        Cart saved = cartRepository.save(cart);
        return cartMapper.toCartResponse(saved, user);
    }

    // ── Get cart ───────────────────────────────────────────────────

    @Override
    public CartResponse getCart(Long userId) {
        log.info("getCart: userId={}", userId);
        Cart cart = findCartOrThrow(userId);
        UserResponse user = userClient.getUserById(userId);
        return cartMapper.toCartResponse(cart, user);
    }

    // ── Update item quantity ───────────────────────────────────────

    @Override
    public CartResponse updateItemQuantity(Long userId, Long productId,
                                           UpdateCartItemRequest request) {
        log.info("updateItemQuantity: userId={}, productId={}, qty={}",
                userId, productId, request.getQuantity());

        Cart cart = findCartOrThrow(userId);

        CartItem item = cart.findItem(productId)
                .orElseThrow(() -> new CartNotFoundException(
                        "Product id=" + productId + " not found in cart for userId=" + userId));

        // Validate stock for the new quantity
        ProductResponse product = productClient.getProductById(productId);
        if (product.getQuantity() < request.getQuantity()) {
            throw new ProductNotAvailableException(
                    productId, request.getQuantity(), product.getQuantity());
        }

        // Refresh price in case it changed since item was added
        item.setUnitPrice(product.getPrice());
        item.setQuantity(request.getQuantity());
        item.recalculate();

        cart.recalculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTtl(redisConfig.getCartTtlSeconds());

        Cart saved = cartRepository.save(cart);
        UserResponse user = userClient.getUserById(userId);
        return cartMapper.toCartResponse(saved, user);
    }

    // ── Remove item ────────────────────────────────────────────────

    @Override
    public CartResponse removeItem(Long userId, Long productId) {
        log.info("removeItem: userId={}, productId={}", userId, productId);

        Cart cart = findCartOrThrow(userId);

        boolean removed = cart.removeItem(productId);
        if (!removed) {
            throw new CartNotFoundException(
                    "Product id=" + productId + " not found in cart for userId=" + userId);
        }

        cart.recalculateTotal();
        cart.setUpdatedAt(LocalDateTime.now());
        cart.setTtl(redisConfig.getCartTtlSeconds());

        Cart saved = cartRepository.save(cart);
        UserResponse user = userClient.getUserById(userId);
        return cartMapper.toCartResponse(saved, user);
    }

    // ── Clear cart ─────────────────────────────────────────────────

    @Override
    public void clearCart(Long userId) {
        log.info("clearCart: userId={}", userId);
        if (!cartRepository.existsById(userId)) {
            throw new CartNotFoundException(userId);
        }
        cartRepository.deleteById(userId);
        log.info("Cart cleared for userId={}", userId);
    }

    // ── Checkout ───────────────────────────────────────────────────

    @Override
    public OrderResponse checkout(Long userId, CheckoutRequest request) {
        log.info("checkout: userId={}", userId);

        Cart cart = findCartOrThrow(userId);

        if (cart.getItems().isEmpty()) {
            throw new CartEmptyException(userId);
        }

        // Map cart items → order items
        List<OrderItemRequest> orderItems = cart.getItems().stream()
                .map(item -> OrderItemRequest.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        // Build CreateOrderRequest for order-service
        CreateOrderRequest orderRequest = CreateOrderRequest.builder()
                .userId(userId)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(
                        request.getBillingAddress() != null
                                ? request.getBillingAddress()
                                : request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .items(orderItems)
                .build();

        // Call order-service via Feign
        OrderResponse orderResponse = orderClient.createOrder(orderRequest);

        if (orderResponse == null) {
            // Fallback returned null — order service is down
            throw new CheckoutFailedException(userId);
        }

        // Clear cart after successful order creation
        cartRepository.deleteById(userId);
        log.info("Checkout complete for userId={}. Order: {}", userId, orderResponse.getOrderNumber());

        return orderResponse;
    }

    // ── Private helpers ────────────────────────────────────────────

    private Cart findCartOrThrow(Long userId) {
        return cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    private Cart createNewCart(Long userId) {
        log.debug("Creating new cart for userId={}", userId);
        return Cart.builder()
                .userId(userId)
                .totalAmount(0.0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .ttl(redisConfig.getCartTtlSeconds())
                .build();
    }

    private void validateProduct(ProductResponse product, Long productId, int requestedQty) {
        if (product == null || !product.isAvailable()) {
            throw new ProductNotAvailableException(productId);
        }
        if (product.getQuantity() < requestedQty) {
            throw new ProductNotAvailableException(productId, requestedQty, product.getQuantity());
        }
    }
}
