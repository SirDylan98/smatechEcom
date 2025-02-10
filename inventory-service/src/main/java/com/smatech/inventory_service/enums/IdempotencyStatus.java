package com.smatech.inventory_service.enums;

public enum IdempotencyStatus {
    NOT_FOUND,
    SUCCESS,
    FAILED,
    DUPLICATE
}
