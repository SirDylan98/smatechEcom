package com.smatech.order_service.exception;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
public class PaymentFailedException extends RuntimeException {
    public PaymentFailedException(String orderId) {
        super("Payment failed for order: " + orderId);
    }
}