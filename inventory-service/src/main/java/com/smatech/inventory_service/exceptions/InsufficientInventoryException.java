package com.smatech.inventory_service.exceptions;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
public class InsufficientInventoryException extends RuntimeException{
    public InsufficientInventoryException(String message) {
        super(message);
    }
}
