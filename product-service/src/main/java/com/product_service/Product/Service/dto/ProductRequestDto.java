package com.product_service.Product.Service.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    @NotBlank(message = "Product name cannot be empty")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Description cannot be empty")
    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Positive(message = "Price must be greater than 0")
    @Max(value = 10000000, message = "Price exceeds allowed limit")
    private double price;

    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 100000, message = "Quantity exceeds allowed limit")
    private int quantity;

    private boolean available;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category cannot exceed 50 characters")
    private String category;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand cannot exceed 50 characters")
    private String brand;

    @NotBlank(message = "Image name is required")
    @Size(max = 255, message = "Image name cannot exceed 255 characters")
    private String imageName;

    @PastOrPresent(message = "Created date cannot be in the future")
    private LocalDateTime createdDate;
}