package com.product_service.Product.Service.service.impl;

import com.product_service.Product.Service.dto.PageResponse;
import com.product_service.Product.Service.dto.ProductRequestDto;
import com.product_service.Product.Service.dto.ProductResponseDto;
import com.product_service.Product.Service.entity.Product;
import com.product_service.Product.Service.exception.ResourceNotFoundException;
import com.product_service.Product.Service.repository.ProductRepository;
import com.product_service.Product.Service.service.ProductService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    public ProductServiceImpl(ModelMapper modelMapper, ProductRepository productRepository) {
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
    }

    @Value("${product.image.path}")
    private String imagePath;

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);


    @Override
    @CacheEvict(value = "products", allEntries = true)
    public ProductResponseDto createProduct(ProductRequestDto productDto) {
        Product product = mapToEntity(productDto);
        product.setCreatedDate(LocalDateTime.now());
        return mapToDto(productRepository.save(product));
    }

    @Override
    @Caching(
            put = {
                    @CachePut(value = "product", key = "#id")
            },
            evict = {
                    @CacheEvict(value = {"productByName","productByBrand","productByCategory","products"}, allEntries = true)
            }
    )
    public ProductResponseDto updateProduct(ProductRequestDto productDto, Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
            product.setName(productDto.getName());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setAvailable(productDto.isAvailable());
            product.setCategory(productDto.getCategory());
            product.setBrand(productDto.getBrand());
            product.setQuantity(productDto.getQuantity());
            product.setImageName(productDto.getImageName());
            product.setCreatedDate(LocalDateTime.now());
            return mapToDto(productRepository.save(product));
    }

    @Override
    @Cacheable(value = "product", key = "#id")
    public ProductResponseDto getProductById(Long id) {
        logger.info("Fetching product from database with id {}", id);
        return mapToDto(productRepository.findById(id).orElseThrow(() -> {
            logger.error("product not found in db {}", id);
            return new ResourceNotFoundException("Product", "id", id);
        }));

    }

    @Override
    @Cacheable(value = "products", key = "#pageNo + '-' + #pageSize + '-' + #sortBy + '-' + #sortDir")
    public PageResponse getAllProducts(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(pageNo,pageSize, sort);
        Page<Product> products = productRepository.findAll(pageable);
        List<Product> content = products.getContent();
        List<ProductResponseDto> productDtos = content.stream().map(this::mapToDto).toList();
        PageResponse pageResponse = new PageResponse();
        pageResponse.setContent(productDtos);
        pageResponse.setPageNo(products.getNumber());
        pageResponse.setPageSize(products.getSize());
        pageResponse.setTotalElements(products.getTotalElements());
        pageResponse.setTotalPages(products.getTotalPages());
        pageResponse.setLast(products.isLast());
        return pageResponse;
    }

    @Override
    @CacheEvict(value = "product", key = "#id")
    public void deleteProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    @Override
    @Cacheable(value="productByName",key ="#keyword")
    public List<ProductResponseDto> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword).stream().map(this::mapToDto).toList();
    }

    @Override
    @Cacheable(value="productByBrand",key="#keyword")
    public List<ProductResponseDto> searchProductByBrand(String keyword) {
        return productRepository.findByBrandContainingIgnoreCase(keyword).stream().map(this::mapToDto).toList();}

    @Override
    @Cacheable(value = "productByCategory",key="#keyword")
    public List<ProductResponseDto> searchProductByCategory(String keyword) {
        return productRepository.findByCategoryContainingIgnoreCase(keyword).stream().map(this::mapToDto).toList();
    }

    @Override
    public String uploadImage(Long id, MultipartFile file) throws IOException {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // 1. Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // 2. Clean filename (important)
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename.replaceAll("\\s+", "_");

        // 3. Ensure directory exists
        Path uploadPath = Paths.get(imagePath);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 4. Correct path join (FIXED)
        Path filePath = uploadPath.resolve(fileName);

        // 5. Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 6. Save in DB
        product.setImageName(fileName);
        productRepository.save(product);

        return fileName;
    }

    @Override
    @Cacheable(value = "productImage", key = "#imageName")
    public byte[] getProductImage(String imageName) throws IOException {

        Path path = Paths.get(imagePath, imageName); // correct path joining

        if (!Files.exists(path)) {
            throw new ResourceNotFoundException("Image", "name", imageName);
        }

        return Files.readAllBytes(path);
    }


    public ProductResponseDto mapToDto(Product product) {
        return modelMapper.map(product, ProductResponseDto.class);
    }

    public Product mapToEntity(ProductRequestDto productDto) {
        return modelMapper.map(productDto, Product.class);
    }
}
