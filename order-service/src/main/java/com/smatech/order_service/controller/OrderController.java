package com.smatech.order_service.controller;

import com.smatech.order_service.dto.CreateOrderRequest;
import com.smatech.order_service.dto.OrderResponse;
import com.smatech.order_service.enums.OrderStatus;
import com.smatech.order_service.processor.OrderProcessor;
import com.smatech.order_service.service.OrderService;
import com.smatech.order_service.utils.ApiResponse;
import com.smatech.order_service.utils.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Order Controller", description = "Endpoints for Order Management")
public class OrderController {
    private final OrderService orderService;
    private final OrderProcessor orderProcess;

    @Operation(summary = "Create a new order")
    @PostMapping("/create")
    public ApiResponse<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        log.info("----> Incoming Create Order request {}", JsonUtil.toJson(request));
        try {
           ApiResponse< OrderResponse> response = orderProcess.createOrder(request);
            return response;
        } catch (Exception e) {
            log.error("Error creating order", e);
            return new ApiResponse<>(null, "Failed to create order: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "Retrieve an order by ID")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String orderId) {
        log.info("----> Fetching order details for orderId {}", orderId);
        try {
            OrderResponse response = orderService.getOrder(orderId);
            return new ApiResponse<>(response, "Order retrieved successfully", HttpStatus.OK.value());
        } catch (Exception e) {
            log.error("Error fetching order details", e);
            return new ApiResponse<>(null, "Failed to retrieve order: " + e.getMessage(), HttpStatus.NOT_FOUND.value());
        }
    }

    @Operation(summary = "Retrieve all orders for a user")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderResponse>> getUserOrders(@PathVariable String userId) {
        log.info("----> Fetching orders for userId {}", userId);
        try {
            List<OrderResponse> response = orderService.getUserOrders(userId);
            return new ApiResponse<>(response, "Orders retrieved successfully", HttpStatus.OK.value());
        } catch (Exception e) {
            log.error("Error fetching user orders", e);
            return new ApiResponse<>(null, "Failed to retrieve user orders: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "Handle payment failure for an order")
    @PostMapping("/payment-failure/{orderId}")
    public ApiResponse<Void> handlePaymentFailure(@PathVariable String orderId) {
        log.info("----> Handling payment failure for orderId {}", orderId);
        try {
            orderService.handlePaymentFailure(orderId);
            return new ApiResponse<>(null, "Payment failure handled successfully", HttpStatus.OK.value());
        } catch (Exception e) {
            log.error("Error handling payment failure", e);
            return new ApiResponse<>(null, "Failed to handle payment failure: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Operation(summary = "Update order status")
    @PutMapping("/update-status/{orderId}")
    public ApiResponse<Void> updateOrderStatus(@PathVariable String orderId, @RequestParam OrderStatus status) {
        log.info("----> Updating order status for orderId {} to {}", orderId, status);
        try {
            orderService.updateOrderStatus(orderId, status);
            return new ApiResponse<>(null, "Order status updated successfully", HttpStatus.OK.value());
        } catch (Exception e) {
            log.error("Error updating order status", e);
            return new ApiResponse<>(null, "Failed to update order status: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
}
