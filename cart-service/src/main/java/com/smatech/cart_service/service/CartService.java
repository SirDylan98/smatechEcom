package com.smatech.cart_service.service;

import com.smatech.cart_service.dto.AddToCartRequest;
import com.smatech.cart_service.dto.CartResponse;
import com.smatech.cart_service.dto.SubtractFromCartRequest;
import com.smatech.cart_service.dto.UpdateCartItemRequest;

public interface CartService {
    CartResponse getCart(String userId);
    CartResponse addToCart(AddToCartRequest request);
    CartResponse updateCartItemQuantity(String userId, String productId, UpdateCartItemRequest request);
    CartResponse removeFromCart(String userId, String productId);
    CartResponse subtractFromCart(SubtractFromCartRequest subtractFromCartRequest);
    void clearCart(String userId);
    CartResponse createCartIfNotExists(String userId);
}
