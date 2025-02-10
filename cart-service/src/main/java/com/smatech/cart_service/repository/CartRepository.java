package com.smatech.cart_service.repository;

import com.smatech.cart_service.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Repository
public interface CartRepository extends JpaRepository<Cart,String> {
}
