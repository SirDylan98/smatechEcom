package com.smatech.product_service.repository;

import com.smatech.product_service.enums.Category;
import com.smatech.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.management.monitor.StringMonitor;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByProductCode(String productCode);
    List<Product> findByProductCodeIn(Set<String> productCodes);
    boolean existsByProductCode(String productCode);
    void deleteByProductCode(String productCode);
    List<Product> findByProductCategory(Category category);
    List<Product> findByProductPriceBetween(double minPrice, double maxPrice);
    List<Product> findByOnSaleTrue();
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.productDescription) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(p.productCategory) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);
}
