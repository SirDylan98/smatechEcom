package com.smatech.payment_service.dto;

import com.smatech.payment_service.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@Data
@Builder
public class PaymentEvent {
    private String orderId;
    private PaymentStatus status;
    private BigDecimal amount;
    private String errorMessage;
    private LocalDateTime timestamp;
}