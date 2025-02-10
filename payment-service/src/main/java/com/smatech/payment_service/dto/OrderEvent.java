package com.smatech.payment_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@Data
@Builder
public class OrderEvent {
    private String orderId;
    private BigDecimal amount;
    private String userId;
    private String currency;
    private String customerEmail;
}
