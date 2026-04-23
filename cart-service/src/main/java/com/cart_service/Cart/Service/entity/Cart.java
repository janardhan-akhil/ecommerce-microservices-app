package com.cart_service.Cart.Service.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@RedisHash("cart")                  // stored under key  cart:<userId>
public class Cart implements Serializable {

    @Id
    private Long userId;             // cart is always scoped to one user

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    private double totalAmount;      // sum of all item totalPrices

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TimeToLive                      // Redis auto-expires the key after this many seconds
    private Long ttl;

    // ── Helpers ────────────────────────────────────────────────────

    /** Find an existing item for the given product, if any. */
    public Optional<CartItem> findItem(Long productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
    }

    /** Recompute totalAmount from all items. */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }

    /** Remove an item by productId. Returns true if something was removed. */
    public boolean removeItem(Long productId) {
        return items.removeIf(i -> i.getProductId().equals(productId));
    }

    /** Total number of individual units in the cart. */
    public int totalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
