package com.cart_service.Cart.Service.repository;



import com.cart_service.Cart.Service.entity.Cart;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends CrudRepository<Cart, Long> {
    // Spring Data Redis provides findById, save, delete, existsById out of the box.
    // Cart key in Redis: cart:<userId>
}