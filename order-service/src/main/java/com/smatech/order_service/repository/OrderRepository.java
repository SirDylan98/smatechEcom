package com.smatech.order_service.repository;

import com.smatech.order_service.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders,String> {
    public List<Orders> findByUserId(String userId);
}
