package com.smatech.product_service.client;

import com.smatech.product_service.dto.Inventory;
import com.smatech.product_service.utils.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@FeignClient(name = "inventory-service", url = "http://localhost:8082/api/v1/carts")
public interface InventoryClient {
    @PostMapping("/initialize")
    public ApiResponse<Inventory> initializeInventory(@RequestBody Inventory request);
    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteInventory(@PathVariable String code);
}
