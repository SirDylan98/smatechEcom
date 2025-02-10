package com.smatech.inventory_service.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smatech.inventory_service.dto.IdempotencyBody;
import com.smatech.inventory_service.dto.InventoryDto;
import com.smatech.inventory_service.enums.IdempotencyStatus;
import com.smatech.inventory_service.exceptions.InsufficientInventoryException;
import com.smatech.inventory_service.model.Inventory;
import com.smatech.inventory_service.service.InventoryService;
import com.smatech.inventory_service.utils.ApiResponse;
import com.smatech.inventory_service.utils.IdempotencyService;
import com.smatech.inventory_service.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryProcessor {
    private final InventoryService inventoryService;

    public ApiResponse<List<Inventory>> addInventoryQuantity(InventoryDto request) {
        log.info("Processing add inventory request: {}", JsonUtil.toJson(request));

        // Check idempotency
        IdempotencyBody idempotencyBody = IdempotencyService.getInventoryServiceIdemp(request.getUniqueKey());
        log.info("Idempotency check result: {}", JsonUtil.toJson(idempotencyBody));

        if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.NOT_FOUND.name())) {
            try {

                List<Inventory> inventoryUpdates = mapDtoToInventoryList(request);


                List<Inventory> response = inventoryService.addInventory(inventoryUpdates);


                storeIdempotencyResponse(request, response, IdempotencyStatus.SUCCESS);

                return new ApiResponse<>(
                        response,
                        "Inventory quantities added successfully",
                        HttpStatus.OK.value()
                );
            } catch (Exception e) {
                log.error("Error processing add inventory request: {}", e.getMessage());
                storeIdempotencyResponse(request, null, IdempotencyStatus.FAILED);
                return new ApiResponse<>(
                        null,
                        "Error adding inventory quantities: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
            }
        } else {
            return handleExistingIdempotencyResponse(idempotencyBody);
        }
    }

    public ApiResponse<List<Inventory>> removeInventoryQuantity(InventoryDto request) {
        log.info("============>Processing remove inventory request: {}", JsonUtil.toJson(request));

        // Check idempotency
        IdempotencyBody idempotencyBody = IdempotencyService.getInventoryServiceIdemp(request.getUniqueKey());
        log.info("================>Idempotency check result: {}", JsonUtil.toJson(idempotencyBody));

        if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.NOT_FOUND.name())) {
            try {

                List<Inventory> inventoryUpdates = mapDtoToInventoryList(request);


                List<Inventory> response = inventoryService.removeInventory(inventoryUpdates);


                storeIdempotencyResponse(request, response, IdempotencyStatus.SUCCESS);

                return new ApiResponse<>(
                        response,
                        "Inventory quantities removed successfully",
                        HttpStatus.OK.value()
                );
            } catch (InsufficientInventoryException e) {
                log.error("Insufficient inventory: {}", e.getMessage());
                storeIdempotencyResponse(request, null, IdempotencyStatus.FAILED);
                return new ApiResponse<>(
                        null,
                        e.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                );
            } catch (Exception e) {
                log.error("Error processing remove inventory request: {}", e.getMessage());
                storeIdempotencyResponse(request, null, IdempotencyStatus.FAILED);
                return new ApiResponse<>(
                        null,
                        "Error removing inventory quantities: " + e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                );
            }
        } else {
            return handleExistingIdempotencyResponse(idempotencyBody);
        }
    }

    private List<Inventory> mapDtoToInventoryList(InventoryDto dto) {

        return dto.getInventoryItemDtos().stream()
                .map(item -> Inventory.builder()
                        .productCode(item.getProductCode())
                        .availableQuantity(item.getAvailableQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private void storeIdempotencyResponse(InventoryDto request, List<Inventory> response, IdempotencyStatus status) {
        IdempotencyService.setInventoryServiceIdomMap(
                request.getUniqueKey(),
                IdempotencyBody.builder()
                        .requestStatus(status.name())
                        .requestBody(JsonUtil.toJson(request))
                        .responseBody(response != null ? JsonUtil.toJson(response) : null)
                        .requestDateTime(LocalDateTime.now())
                        .idemKey(request.getUniqueKey())
                        .build()
        );
    }

    private ApiResponse<List<Inventory>> handleExistingIdempotencyResponse(IdempotencyBody idempotencyBody) {
        if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.SUCCESS.name())) {
            TypeReference<List<Inventory>> typeRef = new TypeReference<List<Inventory>>() {};
            return new ApiResponse<>(
                    JsonUtil.fromJson(idempotencyBody.getResponseBody(), typeRef),
                    "Request processed successfully",
                    HttpStatus.OK.value()
            );
        } else {
            return new ApiResponse<>(
                    null,
                    "Previous request failed",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
}