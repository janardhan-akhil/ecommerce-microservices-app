package com.cart_service.Cart.Service.client;


import com.cart_service.Cart.Service.external.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service",fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    public ProductResponse getProductById(@PathVariable("id") Long id);

//    @PutMapping("/{id}/stock")
//    void updateProductStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}
