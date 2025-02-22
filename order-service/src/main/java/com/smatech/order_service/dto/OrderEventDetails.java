package com.smatech.order_service.dto;

import com.smatech.order_service.enums.Currency;
import com.smatech.order_service.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Data
@Builder
public class OrderEventDetails {
    private String orderId;
    private BigDecimal amount;
    private String userId;
    private String currency;
    private String customerEmail;
}
