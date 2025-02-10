package com.smatech.cart_service.controller;

import com.smatech.cart_service.dto.AddToCartRequest;
import com.smatech.cart_service.dto.CartResponse;
import com.smatech.cart_service.dto.SubtractFromCartRequest;
import com.smatech.cart_service.dto.UpdateCartItemRequest;
import com.smatech.cart_service.processor.CartProcessor;
import com.smatech.cart_service.service.CartService;
import com.smatech.cart_service.utils.ApiResponse;
import com.smatech.cart_service.utils.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart Controller", description = "Endpoints for Cart Management")
public class CartController {
    private final CartService cartService;
    private final CartProcessor cartProcessor;

    @Operation(summary = "Get cart by user ID")
    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getCart(@PathVariable String userId) {
        log.info("----> Fetching cart for user: {}", userId);
        CartResponse cartResponse = cartService.getCart(userId);
        if (cartResponse == null) {
            return new ApiResponse<>(null, "Cart not found", HttpStatus.NOT_FOUND.value());
        }
        return new ApiResponse<>(cartResponse, "Cart retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Add item to cart")
    @PostMapping("/add")
    public ApiResponse<CartResponse> addToCart(@RequestBody @Valid AddToCartRequest request) {
        log.info("----> Adding item to cart: {}", JsonUtil.toJson(request));
        ApiResponse<CartResponse> cartResponse = cartProcessor.addToCart(request);
        return cartResponse ;
    }

    @Operation(summary = "Update cart item quantity")
    @PutMapping("/{userId}/update/{productId}")
    public ApiResponse<CartResponse> updateCartItemQuantity(
            @PathVariable String userId,
            @PathVariable String productId,
            @RequestBody @Valid UpdateCartItemRequest request) {
        log.info("----> Updating cart item quantity for user: {}, product: {}", userId, productId);
        CartResponse cartResponse = cartService.updateCartItemQuantity(userId, productId, request);
        return new ApiResponse<>(cartResponse, "Cart item quantity updated successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Remove item from cart")
    @DeleteMapping("/{userId}/remove/{productId}")
    public ApiResponse<CartResponse> removeFromCart(@PathVariable String userId, @PathVariable String productId) {
        log.info("----> Removing item from cart for user: {}, product: {}", userId, productId);
        CartResponse cartResponse = cartService.removeFromCart(userId, productId);
        return new ApiResponse<>(cartResponse, "Item removed from cart successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Subtract item from cart")
    @PostMapping("/subtract")
    public ApiResponse<CartResponse> subtractFromCart(@RequestBody @Valid SubtractFromCartRequest request) {
        log.info("----> Subtracting item from cart: {}", JsonUtil.toJson(request));
        ApiResponse<CartResponse>  cartResponse = cartProcessor.subtractFromCart(request);
        return cartResponse;
    }

    @Operation(summary = "Clear cart for a user")
    @DeleteMapping("/{userId}/clear")
    public ApiResponse<Void> clearCart(@PathVariable String userId) {
        log.info("----> Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        return new ApiResponse<>(null, "Cart cleared successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Create cart if not exists")
    @PostMapping("/{userId}/create-if-not-exists")
    public ApiResponse<CartResponse> createCartIfNotExists(@PathVariable String userId) {
        log.info("----> Creating cart if not exists for user: {}", userId);
        CartResponse cartResponse = cartService.createCartIfNotExists(userId);
        return new ApiResponse<>(cartResponse, "Cart created or retrieved successfully", HttpStatus.OK.value());
    }
}
