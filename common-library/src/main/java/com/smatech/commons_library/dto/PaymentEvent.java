package com.smatech.commons_library.dto;


import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent {
    private String orderId;
    private PaymentStatus status;
    private String userId;
    private BigDecimal amount;
    private String errorMessage;
    private LocalDateTime timestamp;
}