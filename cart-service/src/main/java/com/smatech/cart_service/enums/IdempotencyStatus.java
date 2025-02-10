package com.smatech.cart_service.enums;

public enum IdempotencyStatus {
    NOT_FOUND,
    SUCCESS,
    FAILED,
    DUPLICATE
}
