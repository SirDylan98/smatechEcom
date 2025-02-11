package com.smatech.inventory_service.service;

import com.smatech.inventory_service.dto.InventoryCheckResult;
import com.smatech.inventory_service.model.Inventory;

import java.util.List;
import java.util.Map;

public interface InventoryService {
    public Inventory initializeInventory(Inventory inventory);

    public Inventory updateInventory(Inventory inventory);

    public Inventory findInventoryByProductCode(String code);

    public void deleteInventory(String code);

    public List<Inventory> findAllInventories();
    public InventoryCheckResult checkInventoryAvailability(Map<String, Integer> requests);

    public List<Inventory> addInventory(List<Inventory> inventory);
    public List<Inventory> removeInventory(List<Inventory> inventory);
    public List<Inventory> reservedInventory(Map<String, Integer> integerMap);
    public List<Inventory> releaseReservedInventory(List<Inventory> inventory);
    public List<Inventory> getAllProductsAtReorderLevel();
    public void notifyInventoryLevel();

}