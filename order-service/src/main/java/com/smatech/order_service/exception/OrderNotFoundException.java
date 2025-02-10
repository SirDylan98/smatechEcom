package com.smatech.order_service.exception;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String orderId) {
        super("Order not found with id: " + orderId);
    }
}