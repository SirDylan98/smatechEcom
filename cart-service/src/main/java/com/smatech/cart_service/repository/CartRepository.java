package com.smatech.cart_service.repository;

import com.smatech.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Repository
public interface CartRepository extends JpaRepository<Cart,String> {
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items i WHERE c.userId = :id AND (i.status = 'ACTIVE' OR i IS NULL)")
    Optional<Cart> getCartByIdWithActiveItems(@Param("id") String id);

    //public Optional<Cart> getCartById(String id);
}
