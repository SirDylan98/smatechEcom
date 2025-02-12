package com.smatech.inventory_service.service;

import com.smatech.commons_library.dto.PaymentEvent;
import com.smatech.commons_library.dto.Topics;
import com.smatech.inventory_service.client.OrderServiceClient;
import com.smatech.inventory_service.dto.*;
import com.smatech.inventory_service.enums.InventoryStatus;
import com.smatech.inventory_service.exceptions.DuplicateResourceException;
import com.smatech.inventory_service.exceptions.InsufficientInventoryException;
import com.smatech.inventory_service.exceptions.InventoryFulfilmentException;
import com.smatech.inventory_service.model.Inventory;
import com.smatech.inventory_service.repository.InventoryRepository;
import com.smatech.inventory_service.utils.ApiResponse;
import com.smatech.inventory_service.utils.JsonUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final OrderServiceClient orderServiceClient;
    @KafkaListener(topics = Topics.PAYMENT_FAILURE_TOPIC, groupId = "inventory-service-group")
    public void handlePaymentFailure(PaymentEvent event, Acknowledgment acknowledgment) {
        log.info(" =====================>Inventory Service received failed payment event: {}", event);
        // Process the failed payment for order management
        processPaymentFailure(event);
        acknowledgment.acknowledge();
    }
    @KafkaListener(topics = Topics.PAYMENT_SUCCESS_TOPIC, groupId = "inventory-service-group")
    public void handlePaymentSuccess(PaymentEvent event, Acknowledgment acknowledgment) {
        log.info("=====================> Inventory Service received Success payment event: {}", event);
        // Process the failed payment for order management
        processPaymentSuccess(event);
        acknowledgment.acknowledge();
    }

    @Override
    public Inventory initializeInventory(Inventory inventory) {
        log.info("===========>Initializing inventory for product: {}", JsonUtil.toJson(inventory));
        // Check if inventory already exists
        if (inventoryRepository.existsByProductCode(inventory.getProductCode())) {
            throw new DuplicateResourceException("Inventory already exists for product: " + inventory.getProductCode());
        }
        inventory.setLastUpdateOnRestock(LocalDateTime.now());
        return inventoryRepository.save(inventory);
    }
    @Override
    public Inventory updateInventory(Inventory inventory) {
        log.info("=============>Updating inventory for product: {}", JsonUtil.toJson(inventory));


        Inventory existingInventory = inventoryRepository.findByProductCode(inventory.getProductCode())
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + inventory.getProductCode()));

        existingInventory.setAvailableQuantity(inventory.getAvailableQuantity());
        existingInventory.setRestockLevel(inventory.getRestockLevel());
        existingInventory.setLastUpdateOnRestock(LocalDateTime.now());
        existingInventory.setReservedQuantity(inventory.getReservedQuantity());

        return inventoryRepository.save(existingInventory);
    }

    @Override
    public Inventory findInventoryByProductCode(String code) {
        log.info("Finding inventory for product: {}", code);
        return inventoryRepository.findByProductCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + code));
    }

    @Override
    public void deleteInventory(String code) {
        log.info("========>Deleting inventory for product: {}", code);
        if (!inventoryRepository.existsByProductCode(code)) {
            throw new EntityNotFoundException("Inventory not found for product: " + code);
        }
        inventoryRepository.deleteByProductCode(code);
    }

    @Override
    public List<Inventory> findAllInventories() {
        log.info("=========>Fetching all inventories");
        return inventoryRepository.findAll();
    }
    @Override
    public InventoryCheckResult checkInventoryAvailability(Map<String, Integer> requests) {
        log.info("=============>Checking inventory availability for {} items", requests.size());
        List<InventoryItemStatus> statuses = new ArrayList<>();
        boolean hasOutOfStock = false;

        for (Map.Entry<String, Integer> request : requests.entrySet()) {
            try {
                Optional<Inventory> inventory = inventoryRepository.findByProductCode(request.getKey());
                InventoryItemStatus status = checkSingleItem(inventory.get(), request.getValue());
                statuses.add(status);

                if (status.getStatus() == InventoryStatus.OUT_OF_STOCK) {
                    hasOutOfStock = true;
                }
            } catch (EntityNotFoundException e) {
                statuses.add(InventoryItemStatus.builder()
                        .productCode(request.getKey())
                        .requestedQuantity(request.getValue())
                        .status(InventoryStatus.NOT_FOUND)
                        .message("Product not found in inventory")
                        .build());
                hasOutOfStock = true;
            }
        }
        if (!hasOutOfStock) {
//            List<Inventory> reserveResponse = reservedInventory(requests);
//            if (reserveResponse.size() != requests.size()) {
//                throw new InsufficientInventoryException("Not all requested items could be reserved");
//            }
        }
        return InventoryCheckResult.builder()
                .itemStatuses(statuses)
                .hasOutOfStock(hasOutOfStock)
                .build();
    }

    private InventoryItemStatus checkSingleItem(Inventory inventory, int requestedQuantity) {
        int availableQty = inventory.getAvailableQuantity();

        InventoryStatus status;
        String message;

        if (availableQty < requestedQuantity) {
            status = InventoryStatus.OUT_OF_STOCK;
            message = "Insufficient quantity available";
        } else {
            status = InventoryStatus.IN_STOCK;
            message = "Item available";
        }

        return InventoryItemStatus.builder()
                .productCode(inventory.getProductCode())
                .requestedQuantity(requestedQuantity)
                .availableQuantity(availableQty)
                .status(status)
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public List<Inventory> addInventory(List<Inventory> inventories) {
        log.info("==========> Adding inventory for {} products", inventories.size());

        List<Inventory> updatedInventories = new ArrayList<>();

        for (Inventory inventory : inventories) {
            Inventory existingInventory = inventoryRepository.findByProductCode(inventory.getProductCode())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + inventory.getProductCode()));

            // Add quantities
            existingInventory.setAvailableQuantity(existingInventory.getAvailableQuantity() + inventory.getAvailableQuantity());
            existingInventory.setLastUpdateOnRestock(LocalDateTime.now());

            updatedInventories.add(inventoryRepository.save(existingInventory));
        }

        return updatedInventories;
    }

    @Override
    @Transactional
    public List<Inventory> removeInventory(List<Inventory> inventories) {
        log.info("==========> Removing inventory for {} products", inventories.size());

        List<Inventory> updatedInventories = new ArrayList<>();

        for (Inventory inventory : inventories) {
            Inventory existingInventory = inventoryRepository.findByProductCode(inventory.getProductCode())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + inventory.getProductCode()));

            // Check if we have enough quantity
            if (existingInventory.getAvailableQuantity() < inventory.getAvailableQuantity()) {
                throw new InsufficientInventoryException("Insufficient inventory for product: " + inventory.getProductCode());
            }

            // Remove quantities
            existingInventory.setAvailableQuantity(existingInventory.getAvailableQuantity() - inventory.getAvailableQuantity());
            existingInventory.setLastUpdateOnRestock(LocalDateTime.now());

            updatedInventories.add(inventoryRepository.save(existingInventory));


        }

        return updatedInventories;
    }

    @Override
    public List<Inventory> reservedInventory(Map<String, Integer> integerMap) {
        List<Inventory> updatedInventories = new ArrayList<>();

        for (Map.Entry<String, Integer> inventory : integerMap.entrySet()) {
            Inventory existingInventory = inventoryRepository.findByProductCode(inventory.getKey())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + inventory.getKey()));

            // Check if we have enough quantity
            if (existingInventory.getAvailableQuantity() < inventory.getValue()) {
                throw new InsufficientInventoryException("Insufficient inventory for product: " + inventory.getKey());
            }
            // Remove quantities
            existingInventory.setAvailableQuantity(existingInventory.getAvailableQuantity() - inventory.getValue());
            existingInventory.setReservedQuantity(existingInventory.getReservedQuantity() + inventory.getValue());
            existingInventory.setLastUpdateOnRestock(LocalDateTime.now());

            updatedInventories.add(inventoryRepository.save(existingInventory));


        }

        return updatedInventories;
    }

    @Override
    public List<Inventory> releaseReservedInventory(List<Inventory> inventoryList) {
        List<Inventory> updatedInventories = new ArrayList<>();

        for (Inventory inventory : inventoryList) {
            Inventory existingInventory = inventoryRepository.findByProductCode(inventory.getProductCode())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + inventory.getProductCode()));
            // Check if we have enough quantity
            if (existingInventory.getAvailableQuantity() < inventory.getAvailableQuantity()) {
                throw new InsufficientInventoryException("Insufficient inventory for product: " + inventory.getProductCode());
            }
            existingInventory.setReservedQuantity(existingInventory.getReservedQuantity() - inventory.getReservedQuantity());
            existingInventory.setAvailableQuantity(existingInventory.getAvailableQuantity() + inventory.getReservedQuantity());
            existingInventory.setLastUpdateOnRestock(LocalDateTime.now());
            updatedInventories.add(inventoryRepository.save(existingInventory));
        }

        return updatedInventories;
    }
    public List<Inventory> releaseInventory(List<Inventory> inventoryList) {
        List<Inventory> updatedInventories = new ArrayList<>();
        for (Inventory inventory : inventoryList) {
            Inventory existingInventory = inventoryRepository.findByProductCode(inventory.getProductCode())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + inventory.getProductCode()));
            existingInventory.setReservedQuantity(existingInventory.getReservedQuantity() - inventory.getReservedQuantity());
            existingInventory.setLastUpdateOnRestock(LocalDateTime.now());
            updatedInventories.add(inventoryRepository.save(existingInventory));
        }

        return updatedInventories;
    }
    public  void processPaymentFailure(PaymentEvent event){
        log.error("Payment failed for order: {}", event.getOrderId());
        // first we get order items and their quantities and release the inventory
        ApiResponse<OrderResponse> orderResponseApiResponse = orderServiceClient.getOrder(event.getOrderId());
        if(orderResponseApiResponse.getStatusCode()!= HttpStatus.OK.value()){

        }
        List< OrderItemResponse> orderItems = orderResponseApiResponse.getBody().getOrderItems();
        List<Inventory> inventoryList = orderItems.stream().map(orderItemResponse -> {
            Inventory inventory = new Inventory();
            inventory.setProductCode(orderItemResponse.getProductId());

            inventory.setReservedQuantity(orderItemResponse.getQuantity());
            //inventory.setRestockLevel(orderItemResponse.getReorderLevel());
            return inventory;
        }).collect(Collectors.toList());
        releaseReservedInventory(inventoryList);



    }
    public  void processPaymentSuccess(PaymentEvent event){
        log.error("Payment Success for order: {}", event.getOrderId());
        // first we get order items and their quantities and release the inventory
        ApiResponse<OrderResponse> orderResponseApiResponse = orderServiceClient.getOrder(event.getOrderId());
        if(orderResponseApiResponse.getStatusCode()!= HttpStatus.OK.value()){
            throw new InventoryFulfilmentException("Error getting order response: " );
        }
        List< OrderItemResponse> orderItems = orderResponseApiResponse.getBody().getOrderItems();
        List<Inventory> inventoryList = orderItems.stream().map(orderItemResponse -> {
            Inventory inventory = new Inventory();
            inventory.setProductCode(orderItemResponse.getProductId());

            inventory.setReservedQuantity(orderItemResponse.getQuantity());
            log.info("=======> inventory for release: {}",JsonUtil.toJson(inventory));
            //inventory.setRestockLevel(orderItemResponse.getReorderLevel());
            return inventory;
        }).collect(Collectors.toList());
        releaseInventory(inventoryList);



    }

    @Override
    public List<Inventory> getAllProductsAtReorderLevel() {
        log.info("==============>Fetching all products at reorder level");
        return inventoryRepository.findByQuantityLessThanEqualToRestockLevel();
    }

    @Override
    public void notifyInventoryLevel() {
        log.info("=============>Checking inventory levels for notifications");
        List<Inventory> lowInventory = getAllProductsAtReorderLevel();

        for (Inventory inventory : lowInventory) {

        }
    }

}
