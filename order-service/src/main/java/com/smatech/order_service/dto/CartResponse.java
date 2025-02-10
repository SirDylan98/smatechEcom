package com.smatech.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private String userId;
    private List<CartItemDTO> items;
    private Double totalAmount;
    private LocalDateTime lastModifiedDate;
}
