package com.cart_service.Cart.Service.service;

import com.cart_service.Cart.Service.client.OrderClient;
import com.cart_service.Cart.Service.client.ProductClient;
import com.cart_service.Cart.Service.client.UserClient;
import com.cart_service.Cart.Service.config.RedisConfig;
import com.cart_service.Cart.Service.dto.request.*;
import com.cart_service.Cart.Service.dto.response.CartResponse;
import com.cart_service.Cart.Service.entity.Cart;
import com.cart_service.Cart.Service.entity.CartItem;
import com.cart_service.Cart.Service.external.OrderResponse;
import com.cart_service.Cart.Service.external.ProductResponse;
import com.cart_service.Cart.Service.external.UserResponse;
import com.cart_service.Cart.Service.mapper.CartMapper;
import com.cart_service.Cart.Service.repository.CartRepository;
import com.cart_service.Cart.Service.service.impl.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private UserClient userClient;
    @Mock private ProductClient productClient;
    @Mock private OrderClient orderClient;
    @Mock private CartMapper cartMapper;
    @Mock private RedisConfig redisConfig;

    @InjectMocks
    private CartServiceImpl cartService;

    private UserResponse user;
    private ProductResponse product;
    private Cart cart;

    @BeforeEach
    void setup() {
        user = new UserResponse();
        product = new ProductResponse();
        product.setId(1L);
        product.setName("Phone");
        product.setPrice(100.0);
        product.setQuantity(10);
        product.setAvailable(true);

        cart = Cart.builder()
                .userId(1L)
                .build();
    }

    // ✅ ADD TO CART
    @Test
    void testAddToCart_NewCart() {
        AddToCartRequest request = new AddToCartRequest(1L, 2);

        when(userClient.getUserById(1L)).thenReturn(user);
        when(productClient.getProductById(1L)).thenReturn(product);
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());
        when(redisConfig.getCartTtlSeconds()).thenReturn(3600L);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);
        when(cartMapper.toCartResponse(any(), eq(user))).thenReturn(new CartResponse());

        CartResponse response = cartService.addToCart(1L, request);

        assertNotNull(response);
        verify(cartRepository).save(any(Cart.class));
    }

    // ✅ GET CART
    @Test
    void testGetCart() {
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(userClient.getUserById(1L)).thenReturn(user);
        when(cartMapper.toCartResponse(cart, user)).thenReturn(new CartResponse());

        CartResponse response = cartService.getCart(1L);

        assertNotNull(response);
    }

    // ✅ UPDATE ITEM QUANTITY
    @Test
    void testUpdateItemQuantity() {
        CartItem item = CartItem.builder()
                .productId(1L)
                .quantity(1)
                .unitPrice(100.0)
                .build();

        cart.getItems().add(item);

        UpdateCartItemRequest request = new UpdateCartItemRequest(3);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(productClient.getProductById(1L)).thenReturn(product);
        when(redisConfig.getCartTtlSeconds()).thenReturn(3600L);
        when(cartRepository.save(any())).thenReturn(cart);
        when(userClient.getUserById(1L)).thenReturn(user);
        when(cartMapper.toCartResponse(any(), any())).thenReturn(new CartResponse());

        CartResponse response =
                cartService.updateItemQuantity(1L, 1L, request);

        assertNotNull(response);
        verify(cartRepository).save(cart);
    }

    // ✅ REMOVE ITEM
    @Test
    void testRemoveItem() {
        CartItem item = CartItem.builder()
                .productId(1L)
                .quantity(1)
                .unitPrice(100.0)
                .build();

        cart.getItems().add(item);

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(redisConfig.getCartTtlSeconds()).thenReturn(3600L);
        when(cartRepository.save(any())).thenReturn(cart);
        when(userClient.getUserById(1L)).thenReturn(user);
        when(cartMapper.toCartResponse(any(), any())).thenReturn(new CartResponse());

        CartResponse response = cartService.removeItem(1L, 1L);

        assertNotNull(response);
        verify(cartRepository).save(cart);
    }

    // ✅ CLEAR CART
    @Test
    void testClearCart() {
        when(cartRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cartRepository).deleteById(1L);

        cartService.clearCart(1L);

        verify(cartRepository).deleteById(1L);
    }

    // ✅ CHECKOUT
    @Test
    void testCheckout() {
        CartItem item = CartItem.builder()
                .productId(1L)
                .quantity(2)
                .build();

        cart.getItems().add(item);

        CheckoutRequest request = new CheckoutRequest();
        request.setShippingAddress("Address");
        request.setPaymentMethod("COD");

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderNumber("ORD123");

        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart));
        when(orderClient.createOrder(any())).thenReturn(orderResponse);

        OrderResponse response = cartService.checkout(1L, request);

        assertNotNull(response);
        verify(cartRepository).deleteById(1L);
    }
}