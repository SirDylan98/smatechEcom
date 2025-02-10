package com.smatech.order_service.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smatech.order_service.dto.CartResponse;
import com.smatech.order_service.dto.CreateOrderRequest;
import com.smatech.order_service.dto.IdempotencyBody;
import com.smatech.order_service.dto.OrderResponse;
import com.smatech.order_service.enums.IdempotencyStatus;
import com.smatech.order_service.service.OrderService;
import com.smatech.order_service.utils.ApiResponse;
import com.smatech.order_service.utils.IdempotencyService;
import com.smatech.order_service.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderProcessor {
    private final OrderService orderService;
    public ApiResponse<OrderResponse> createOrder(CreateOrderRequest request) {
        // Create unique key for idempotency check
        String idempotencyKey = request.getUniqueKey();

        IdempotencyBody idempotencyBody = IdempotencyService.getOrderServiceIdemp(idempotencyKey);
        log.info("Idempotency check for adding to cart: {}", JsonUtil.toJson(idempotencyBody));

        if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.NOT_FOUND.name())) {
            // New request - process it
            try {
                OrderResponse response = orderService.createOrder(request);

                // Store the result for future idempotency checks
                IdempotencyService.setOrderServiceIdomMap(idempotencyKey,
                        IdempotencyBody.builder()
                                .requestStatus(IdempotencyStatus.SUCCESS.name())
                                .requestBody(JsonUtil.toJson(request))
                                .responseBody(JsonUtil.toJson(response))
                                .requestDateTime(LocalDateTime.now())
                                .idemKey(idempotencyKey)
                                .build());

                return new ApiResponse<>(response,
                        "Order Created successfully",
                        HttpStatus.OK.value());
            } catch (Exception e) {
                log.error("Error creating Order", e);

                // Store the failure for idempotency
                IdempotencyService.setOrderServiceIdomMap(idempotencyKey,
                        IdempotencyBody.builder()
                                .requestStatus(IdempotencyStatus.FAILED.name())
                                .requestBody(JsonUtil.toJson(request))
                                .responseBody(e.getMessage())
                                .requestDateTime(LocalDateTime.now())
                                .idemKey(idempotencyKey)
                                .build());

                return new ApiResponse<>(null,
                        "Error adding item to cart",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else {
            // Request was already processed - return stored result
            if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.SUCCESS.name())) {
                TypeReference<OrderResponse> typeRef = new TypeReference<OrderResponse>() {};
                return new ApiResponse<>(
                        JsonUtil.fromJson(idempotencyBody.getResponseBody(), typeRef),
                        "Order created successfully",
                        HttpStatus.OK.value());
            } else  {
                return new ApiResponse<>(null,
                        "Error in creating Order",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }
}
