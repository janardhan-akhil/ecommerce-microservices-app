package com.product_service.Product.Service.repository;

import com.product_service.Product.Service.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByBrandContainingIgnoreCase(String keyword);
    List<Product> findByCategoryContainingIgnoreCase(String keyword);
}
