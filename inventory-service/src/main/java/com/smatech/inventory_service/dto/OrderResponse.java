package com.smatech.inventory_service.dto;


import com.smatech.inventory_service.enums.Currency;
import com.smatech.inventory_service.enums.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderResponse {
    private String orderId;
    private String userId;
    private OrderStatus orderStatus;
    private List<OrderItemResponse> orderItems;
    private Double totalAmount;
    private Currency currency;
    private LocalDateTime createdDate;
    private String shippingAddress;
    private String checkoutUrl;
}
