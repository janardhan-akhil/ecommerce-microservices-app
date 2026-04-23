package com.product_service.Product.Service.service;

import com.product_service.Product.Service.dto.PageResponse;
import com.product_service.Product.Service.dto.ProductRequestDto;
import com.product_service.Product.Service.dto.ProductResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    public ProductResponseDto createProduct(ProductRequestDto productDto);
    public ProductResponseDto updateProduct(ProductRequestDto productDto, Long id);
    public ProductResponseDto getProductById(Long id);
    public PageResponse getAllProducts(int pageNo, int pageSize, String sortBy, String sortDir);
    public void deleteProductById(Long id);
    public List<ProductResponseDto> searchProducts(String keyword);
    public List<ProductResponseDto> searchProductByBrand(String keyword);
    public List<ProductResponseDto> searchProductByCategory(String keyword);
    public String uploadImage(Long id, MultipartFile file) throws IOException;
    public byte[] getProductImage(String imageName) throws IOException;

}
