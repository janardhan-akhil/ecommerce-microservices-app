package com.cart_service.Cart.Service.client;


import com.cart_service.Cart.Service.external.ProductResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductResponse getProductById(Long id) {
        log.warn("ProductService is unavailable. Returning fallback for productId: {}", id);
        return ProductResponse.builder()
                .id(id)
                .name("Unknown Product")
                .price(0.0)
                .quantity(0)
                .available(false)
                .build();
    }

//    @Override
//    public void updateProductStock(Long id, int quantity) {
//        log.warn("ProductService is unavailable. Could not update stock for productId: {}", id);
//    }
}
