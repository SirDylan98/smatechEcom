package com.smatech.product_service.repository;

import com.smatech.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.management.monitor.StringMonitor;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, StringMonitor> {
}
