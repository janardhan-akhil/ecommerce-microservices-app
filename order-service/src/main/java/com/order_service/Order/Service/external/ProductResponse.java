package com.order_service.Order.Service.external;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String description;
    private double price;          // matches Product.price (double)
    private int quantity;          // matches Product.quantity (int)
    private boolean available;
    private String category;
    private String brand;
    private String imageName;
    private LocalDateTime createdDate;

}
