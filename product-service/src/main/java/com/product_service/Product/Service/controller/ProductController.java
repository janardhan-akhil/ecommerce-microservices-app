package com.product_service.Product.Service.controller;

import com.product_service.Product.Service.dto.PageResponse;
import com.product_service.Product.Service.dto.ProductRequestDto;
import com.product_service.Product.Service.dto.ProductResponseDto;
import com.product_service.Product.Service.entity.Product;
import com.product_service.Product.Service.service.ProductService;
import com.product_service.Product.Service.utility.AppConstants;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(@Valid @RequestBody ProductRequestDto productDto) {
        return ResponseEntity.ok(productService.createProduct(productDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(@Valid @RequestBody ProductRequestDto productDto, @PathVariable Long id) {
        return ResponseEntity.ok(productService.updateProduct(productDto,id));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        log.info("Get product by id {}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse> getAllProducts(
            @RequestParam(value = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        return ResponseEntity.ok(productService.getAllProducts(pageNo, pageSize, sortBy, sortDir));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProductById(@PathVariable Long id) {
        productService.deleteProductById(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDto>> searchProductById(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    @GetMapping("/brand")
    public ResponseEntity<List<ProductResponseDto>> searchProductByBrand(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProductByBrand(keyword));
    }

    @GetMapping("/category")
    public ResponseEntity<List<ProductResponseDto>> searchProductByCategory(@RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProductByCategory(keyword));
    }

    @PostMapping("/image/{id}")
    public ResponseEntity<String> uploadImage(@PathVariable Long id, @RequestParam("image") MultipartFile file) throws IOException, IOException {
        return ResponseEntity.ok("Image uploaded Successfully"+":"+productService.uploadImage(id, file));
    }

    @GetMapping("/image/{imageName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageName) throws IOException {

        byte[] image = productService.getProductImage(imageName);

        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(image);
    }
}
