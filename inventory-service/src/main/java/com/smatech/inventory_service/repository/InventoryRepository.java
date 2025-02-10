package com.smatech.inventory_service.repository;

import com.smatech.inventory_service.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,String> {
    Optional<Inventory> findByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
    void deleteByProductCode(String productCode);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.restockLevel")
    List<Inventory> findByQuantityLessThanEqualToRestockLevel();
}
