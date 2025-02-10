package com.smatech.product_service.repository;

import com.smatech.product_service.enums.Category;
import com.smatech.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.management.monitor.StringMonitor;
import java.util.List;
import java.util.Optional;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, StringMonitor> {
    Optional<Product> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    void deleteByProductCode(String productCode);
    List<Product> findByProductCategory(Category category);
    List<Product> findByProductPriceBetween(double minPrice, double maxPrice);
    List<Product> findByOnSaleTrue();
    List<Product> findByProductNameLikeOrProductDescriptionLike(String name, String description);
}
