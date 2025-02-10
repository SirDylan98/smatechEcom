package com.smatech.order_service.enums;

public enum IdempotencyStatus {
    NOT_FOUND,
    SUCCESS,
    FAILED,
    DUPLICATE
}
