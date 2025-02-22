package com.smatech.order_service.clients;

import com.smatech.order_service.dto.Inventory;
import com.smatech.order_service.dto.InventoryCheckResult;
import com.smatech.order_service.utils.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
@FeignClient(name = "inventory-service", url = "http://localhost:8081/api/v1/inventory")
public interface InventoryServiceClient {
    @PostMapping("/check-availability")
    public ApiResponse<InventoryCheckResult> checkInventoryAvailability(
            @RequestBody  Map<String, Integer> requests);
    @PostMapping("/reserve")
    public ApiResponse<List<Inventory>> reserveInventory(
            @RequestBody  Map<String, Integer> requests);
}
