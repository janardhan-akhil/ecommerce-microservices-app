package com.cart_service.Cart.Service.exception;


public class ProductNotAvailableException extends RuntimeException {

    public ProductNotAvailableException(Long productId) {
        super("Product is not available for purchase: id=" + productId);
    }

    public ProductNotAvailableException(Long productId, int requested, int inStock) {
        super(String.format(
                "Insufficient stock for product id=%d. Requested: %d, in stock: %d",
                productId, requested, inStock
        ));
    }
}
