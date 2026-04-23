package com.product_service.Product.Service.service;

import com.product_service.Product.Service.dto.PageResponse;
import com.product_service.Product.Service.dto.ProductRequestDto;
import com.product_service.Product.Service.dto.ProductResponseDto;
import com.product_service.Product.Service.entity.Product;
import com.product_service.Product.Service.exception.ResourceNotFoundException;
import com.product_service.Product.Service.repository.ProductRepository;
import com.product_service.Product.Service.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {


    @Mock
    private ProductRepository  productRepository;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private ProductServiceImpl productServiceImpl;



    @Test
    void shouldCreateProduct(){
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setName("Laptop");


        Product product = new Product();
        product.setName("Laptop");

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Laptop");

        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setName("Laptop");


        Mockito.when(modelMapper.map(requestDto, Product.class)).thenReturn(product);
        Mockito.when(modelMapper.map(savedProduct, ProductResponseDto.class)).thenReturn(responseDto);
        Mockito.when(productRepository.save(product)).thenReturn(savedProduct);

        ProductResponseDto result = productServiceImpl.createProduct(requestDto);
        Assertions.assertEquals("Laptop",result.getName());


    }

    @Test
    void shouldReturnProductById(){
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        product.setName("Laptop");

        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setName("Laptop");
        Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(product));
        Mockito.when(modelMapper.map(product, ProductResponseDto.class)).thenReturn(responseDto);

        ProductResponseDto result = productServiceImpl.getProductById(id);
        Assertions.assertEquals("Laptop",result.getName());
    }

    @Test
    void shouldThrowExceptionWhenProductNotFound(){
        Long id = 1L;
        Mockito.when(productRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> productServiceImpl.getProductById(id));
    }

    @Test
    void shouldDeleteProductById(){
        Long id = 1L;
        Product product = new Product();
        product.setId(id);
        Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(product));
        productServiceImpl.deleteProductById(id);
        Mockito.verify(productRepository, Mockito.times(1)).delete(product);

    }

    @Test
    void shouldSearchByProductName(){
        String keyword = "Laptop";
        Product product = new Product();
        product.setName("Laptop");
        ProductResponseDto dto = new ProductResponseDto();
        dto.setName("Laptop");

        Mockito.when(productRepository.findByNameContainingIgnoreCase(keyword)).thenReturn(List.of(product));
        Mockito.when(modelMapper.map(product, ProductResponseDto.class)).thenReturn(dto);
        List<ProductResponseDto> productResponseDtos = productServiceImpl.searchProducts(keyword);
        Assertions.assertEquals(1,productResponseDtos.size());
    }

    @Test
    void shouldSearchProductByBrand(){
        String brand = "Apple";
        Product product = new Product();
        product.setName("Apple");
        ProductResponseDto dto = new ProductResponseDto();
        Mockito.when(productRepository.findByBrandContainingIgnoreCase(brand)).thenReturn(List.of(product));
        Mockito.when(modelMapper.map(product, ProductResponseDto.class)).thenReturn(dto);
        List<ProductResponseDto> productResponseDtos = productServiceImpl.searchProductByBrand(brand);
        Assertions.assertEquals(1,productResponseDtos.size());
    }

    @Test
    void shouldSearchProductByCategory(){
        String category = "Apple";
        Product product = new Product();
        product.setName("Apple");
        ProductResponseDto dto = new ProductResponseDto();
        Mockito.when(productRepository.findByCategoryContainingIgnoreCase(category)).thenReturn(List.of(product));
        Mockito.when(modelMapper.map(product, ProductResponseDto.class)).thenReturn(dto);
        List<ProductResponseDto> productResponseDtos = productServiceImpl.searchProductByCategory(category);
        Assertions.assertEquals(1,productResponseDtos.size());
    }

    @Test
    void shouldUpdateProduct() {

        Long id = 1L;

        ProductRequestDto request = new ProductRequestDto();
        request.setName("Updated Laptop");

        Product product = new Product();
        product.setId(id);

        Product savedProduct = new Product();
        savedProduct.setId(id);
        savedProduct.setName("Updated Laptop");

        ProductResponseDto response = new ProductResponseDto();
        response.setName("Updated Laptop");

        Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(product));
        Mockito.when(productRepository.save(product)).thenReturn(savedProduct);
        Mockito.when(modelMapper.map(savedProduct, ProductResponseDto.class)).thenReturn(response);

        ProductResponseDto result = productServiceImpl.updateProduct(request, id);

        Assertions.assertEquals("Updated Laptop", result.getName());
    }

    @Test
    void shouldReturnPagedProducts() {

        int pageNo = 0;
        int pageSize = 5;
        String sortBy = "name";
        String sortDir = "asc";

        Product product1 = new Product();
        product1.setId(1L);
         product1.setName("Laptop");
        Product product2 = new Product();
        product2.setId(2L);
        product2.setName("Mobile");

        List<Product> productList = List.of(product1,product2);

        Page<Product> page = new PageImpl<>(productList);

        Mockito.when(productRepository.findAll(Mockito.any(Pageable.class)))
                .thenReturn(page);

        PageResponse response =
                productServiceImpl.getAllProducts(pageNo, pageSize, sortBy, sortDir);

        Assertions.assertEquals(2, response.getContent().size());
        Assertions.assertEquals(2, response.getTotalElements());
        Assertions.assertEquals(pageNo, response.getPageNo());
    }

    @Test
    void shouldUploadImage() throws Exception {

        Long id = 1L;

        Product product = new Product();
        product.setId(id);

        MultipartFile file = Mockito.mock(MultipartFile.class);

        Mockito.when(productRepository.findById(id))
                .thenReturn(Optional.of(product));

        Mockito.when(file.getOriginalFilename())
                .thenReturn("test.png");

        Mockito.when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("data".getBytes()));

        // create temp directory
        Path tempDir = Files.createTempDirectory("test-images");

        ReflectionTestUtils.setField(productServiceImpl, "imagePath", tempDir.toString() + "/");

        String fileName = productServiceImpl.uploadImage(id, file);

        Assertions.assertEquals("test.png", fileName);

        Mockito.verify(productRepository).save(product);
    }

    @Test
    void shouldThrowExceptionWhenUploadImageIsNull(){
        Product product = new Product();
        Long id = 1L;
        MultipartFile file = Mockito.mock(MultipartFile.class);
        Mockito.when(productRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productServiceImpl.uploadImage(id, file);
        });
    }

    @Test
    void shouldReturnProductImage() throws IOException {

        String imageName = "test.jpg";

        Path tempDir = Files.createTempDirectory("images");
        Path imageFile = tempDir.resolve(imageName);

        Files.write(imageFile, "image-data".getBytes());

        ReflectionTestUtils.setField(productServiceImpl, "imagePath", tempDir.toString());

        byte[] result = productServiceImpl.getProductImage(imageName);

        Assertions.assertNotNull(result);
        Assertions.assertEquals("image-data", new String(result));
    }

    @Test
    void shouldThrowExceptionWhenProductImageIsNull() throws IOException {
        String imageName = "test.jpg";
        Path tempDir = Files.createTempDirectory("images");
        ReflectionTestUtils.setField(productServiceImpl, "imagePath", tempDir.toString());
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            productServiceImpl.getProductImage(imageName);
        });
    }

    @Test
    void shouldProductMapToDto(){
        Product product = new Product();
        product.setName("Laptop");
        ProductResponseDto dto = new ProductResponseDto();
        dto.setName("Laptop");
        Mockito.when(modelMapper.map(product, ProductResponseDto.class)).thenReturn(dto);
        ProductResponseDto result = productServiceImpl.mapToDto(product);
        Assertions.assertEquals("Laptop",result.getName());

    }

    @Test
    void shouldProductMapToEntity(){
        Product product = new Product();
        product.setName("Laptop");
        ProductRequestDto dto = new ProductRequestDto();
        dto.setName("Laptop");
        Mockito.when(modelMapper.map(dto, Product.class)).thenReturn(product);
        Product result = productServiceImpl.mapToEntity(dto);
        Assertions.assertEquals("Laptop",result.getName());
    }


}
