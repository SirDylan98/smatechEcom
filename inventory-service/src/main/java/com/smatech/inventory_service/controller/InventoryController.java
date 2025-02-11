package com.smatech.inventory_service.controller;

import com.smatech.inventory_service.dto.InventoryCheckResult;
import com.smatech.inventory_service.dto.InventoryDto;
import com.smatech.inventory_service.model.Inventory;
import com.smatech.inventory_service.processor.InventoryProcessor;
import com.smatech.inventory_service.service.InventoryService;
import com.smatech.inventory_service.utils.ApiResponse;
import com.smatech.inventory_service.utils.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Controller", description = "Endpoints for Inventory Management")
public class InventoryController {
    private final InventoryService inventoryService;
    private final InventoryProcessor inventoryProcessor;

    @Operation(summary = "Initialize new inventory")
    @PostMapping("/initialize")
    public ApiResponse<Inventory> initializeInventory(@RequestBody  Inventory request) {
        log.info("----> Incoming Initialize Inventory request {}", JsonUtil.toJson(request));
        Inventory response = inventoryService.initializeInventory(request);
        return new ApiResponse<>(response, "Inventory initialized successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Update existing inventory")
    @PutMapping("/update")
    public ApiResponse<Inventory> updateInventory(@RequestBody @Valid Inventory request) {
        log.info("----> Incoming Update Inventory request {}", JsonUtil.toJson(request));
        Inventory response = inventoryService.updateInventory(request);
        if (response == null) {
            return new ApiResponse<>(null, "Inventory not found", HttpStatus.NOT_FOUND.value());
        }
        return new ApiResponse<>(response, "Inventory updated successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Find inventory by product code")
    @GetMapping("/{code}")
    public ApiResponse<Inventory> findInventoryByProductCode(@PathVariable String code) {
        log.info("----> Fetching inventory for product code: {}", code);
        Inventory response = inventoryService.findInventoryByProductCode(code);
        if (response == null) {
            return new ApiResponse<>(null, "Inventory not found", HttpStatus.NOT_FOUND.value());
        }
        return new ApiResponse<>(response, "Inventory retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Delete inventory by product code")
    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteInventory(@PathVariable String code) {
        log.info("----> Deleting inventory for product code: {}", code);
        inventoryService.deleteInventory(code);
        return new ApiResponse<>(null, "Inventory deleted successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Retrieve all inventories")
    @GetMapping
    public ApiResponse<List<Inventory>> findAllInventories() {
        log.info("----> Fetching all inventories");
        List<Inventory> response = inventoryService.findAllInventories();
        return new ApiResponse<>(response, "Inventories retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Add inventory items")
    @PostMapping("/add")
    public ApiResponse<List<Inventory>> addInventory(@RequestBody @Valid InventoryDto request) {
        log.info("----> Incoming Add Inventory request for {} items",JsonUtil.toJson(request)) ;
        ApiResponse<List<Inventory>> response = inventoryProcessor.addInventoryQuantity(request);
        return response;
    }

    @Operation(summary = "Remove inventory items")
    @PostMapping("/remove")
    public ApiResponse<List<Inventory>> removeInventory(@RequestBody @Valid InventoryDto request) {
        log.info("----> Incoming Subtracting Inventory request for {} items",JsonUtil.toJson(request)) ;
        ApiResponse<List<Inventory>> response = inventoryProcessor.removeInventoryQuantity(request);
        return response;
    }

    @Operation(summary = "Get all products at reorder level")
    @GetMapping("/reorder-level")
    public ApiResponse<List<Inventory>> getAllProductsAtReorderLevel() {
        log.info("----> Fetching all products at reorder level");
        List<Inventory> response = inventoryService.getAllProductsAtReorderLevel();
        return new ApiResponse<>(response, "Reorder level products retrieved successfully", HttpStatus.OK.value());
    }


    @Operation(summary = "Check inventory availability for multiple products")
    @PostMapping("/check-availability")
    public ApiResponse<InventoryCheckResult> checkInventoryAvailability(
            @RequestBody @Valid Map<String, Integer> requests) {
        log.info("----> Incoming Check Inventory Availability request {}", JsonUtil.toJson(requests));

        InventoryCheckResult result = inventoryService.checkInventoryAvailability(requests);

        String message = result.isHasOutOfStock()
                ? "Some items are out of stock"
                : "All items available";

        return new ApiResponse<>(
                result,
                message,
                HttpStatus.OK.value()
        );
    }

    @Operation(summary = "Reserve inventory for multiple products")
    @PostMapping("/reserve")
    public ApiResponse<List<Inventory>> reserveInventory(
            @RequestBody @Valid Map<String, Integer> requests) {
        log.info("----> Incoming Reserve Inventory request {}", JsonUtil.toJson(requests));


        List<Inventory> result = inventoryService.reservedInventory(requests);

        return new ApiResponse<>(
                result,
                "Inventory reserved successfully",
                HttpStatus.OK.value()
        );
    }

    @Operation(summary = "Release reserved inventory")
    @PostMapping("/release")
    public ApiResponse<List<Inventory>> releaseInventory(
            @RequestBody @Valid Map<String, Integer> requests) {
        log.info("----> Incoming Release Inventory request {}", JsonUtil.toJson(requests));

        // Convert map to list of Inventory objects
        List<Inventory> inventoryList = requests.entrySet().stream()
                .map(entry -> Inventory.builder()
                        .productCode(entry.getKey())
                        .reservedQuantity(entry.getValue())
                        .build())
                .collect(Collectors.toList());

        List<Inventory> result = inventoryService.releaseReservedInventory(inventoryList);

        return new ApiResponse<>(
                result,
                "Inventory released successfully",
                HttpStatus.OK.value()
        );
    }
}