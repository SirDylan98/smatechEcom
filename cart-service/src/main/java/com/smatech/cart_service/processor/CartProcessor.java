package com.smatech.cart_service.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smatech.cart_service.dto.AddToCartRequest;
import com.smatech.cart_service.dto.CartResponse;
import com.smatech.cart_service.dto.IdempotencyBody;
import com.smatech.cart_service.dto.SubtractFromCartRequest;
import com.smatech.cart_service.enums.IdempotencyStatus;
import com.smatech.cart_service.exceptions.CartItemNotFoundException;
import com.smatech.cart_service.service.CartService;
import com.smatech.cart_service.utils.ApiResponse;
import com.smatech.cart_service.utils.IdempotencyService;
import com.smatech.cart_service.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CartProcessor {
    private final CartService cartService;

    public ApiResponse<CartResponse> addToCart( AddToCartRequest request) {
        // Create unique key for idempotency check
        String idempotencyKey = request.getUniqueKey();

        IdempotencyBody idempotencyBody = IdempotencyService.getCartServiceIdemp(idempotencyKey);
        log.info("Idempotency check for adding to cart: {}", JsonUtil.toJson(idempotencyBody));

        if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.NOT_FOUND.name())) {
            // New request - process it
            try {
                CartResponse response = cartService.addToCart( request);

                // Store the result for future idempotency checks
                IdempotencyService.setCartServiceIdomMap(idempotencyKey,
                        IdempotencyBody.builder()
                                .requestStatus(IdempotencyStatus.SUCCESS.name())
                                .requestBody(JsonUtil.toJson(request))
                                .responseBody(JsonUtil.toJson(response))
                                .requestDateTime(LocalDateTime.now())
                                .idemKey(idempotencyKey)
                                .build());

                return new ApiResponse<>(response,
                        "Item added to cart successfully",
                        HttpStatus.OK.value());
            } catch (Exception e) {
                log.error("Error adding item to cart", e);

                // Store the failure for idempotency
                IdempotencyService.setCartServiceIdomMap(idempotencyKey,
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
                TypeReference<CartResponse> typeRef = new TypeReference<CartResponse>() {};
                return new ApiResponse<>(
                        JsonUtil.fromJson(idempotencyBody.getResponseBody(), typeRef),
                        "Item added to cart successfully",
                        HttpStatus.OK.value());
            } else  {
                return new ApiResponse<>(null,
                        "Error adding item to cart",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }

    public ApiResponse<CartResponse> subtractFromCart(SubtractFromCartRequest request) {
        // Create unique key for idempotency check
        String idempotencyKey = request.getUniqueKey();

        IdempotencyBody idempotencyBody = IdempotencyService.getCartServiceIdemp(idempotencyKey);
        log.info("Idempotency check for removing from cart: {}", JsonUtil.toJson(idempotencyBody));

        if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.NOT_FOUND.name())) {
            // New request - process it
            try {
                CartResponse response = cartService.subtractFromCart(request);

                // Store the result for future idempotency checks
                IdempotencyService.setCartServiceIdomMap(idempotencyKey,
                        IdempotencyBody.builder()
                                .requestStatus(IdempotencyStatus.SUCCESS.name())
                                .requestBody(JsonUtil.toJson(request))
                                .responseBody(JsonUtil.toJson(response))
                                .requestDateTime(LocalDateTime.now())
                                .idemKey(idempotencyKey)
                                .build());

                return new ApiResponse<>(response,
                        "Item Quantity subtraction from cart successfully",
                        HttpStatus.OK.value());
            } catch (Exception e) {
                log.error("Error removing item from cart", e);

                // Store the failure for idempotency
                IdempotencyService.setCartServiceIdomMap(idempotencyKey,
                        IdempotencyBody.builder()
                                .requestStatus(IdempotencyStatus.FAILED.name())
                                .requestBody(JsonUtil.toJson(request))
                                .responseBody(e.getMessage())
                                .requestDateTime(LocalDateTime.now())
                                .idemKey(idempotencyKey)
                                .build());

                return new ApiResponse<CartResponse>(null,
                        "Error subtraction quantity item from cart",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        } else {
            // Request was already processed - return stored result
            if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.SUCCESS.name())) {
                TypeReference<CartResponse> typeRef = new TypeReference<CartResponse>() {};
                return new ApiResponse<>(
                        JsonUtil.fromJson(idempotencyBody.getResponseBody(), typeRef),
                        "Item removed from cart successfully",
                        HttpStatus.OK.value());
            } else  {
                return new ApiResponse<>(null,
                        "Error removing item from cart",
                        HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }
}