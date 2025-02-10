package com.smatech.cart_service.exceptions;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
public class CartItemNotFoundException extends RuntimeException{
    public CartItemNotFoundException(String productId, String userId) {
        super("Product " + productId + " not found in cart for user: " + userId);
    }
}
