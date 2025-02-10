package com.smatech.order_service.repository;

import com.smatech.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,String> {
    public List<Order> findByUserId(String userId);
}
