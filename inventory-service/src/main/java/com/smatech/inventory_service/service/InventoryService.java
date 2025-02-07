package com.smatech.inventory_service.service;

import com.smatech.inventory_service.model.Inventory;

import java.util.List;

public interface InventoryService {
    public Inventory initializeInventory(Inventory inventory);

    public Inventory updateInventory(Inventory inventory);

    public Inventory findInventoryByProductCode(String code);

    public void deleteInventory(String code);

    public List<Inventory> findAllInventories();

    public List<Inventory> addInventory(List<Inventory> inventory);
    public List<Inventory> removeInventory(List<Inventory> inventory);
    public List<Inventory> getAllProductsAtReorderLevel();

}