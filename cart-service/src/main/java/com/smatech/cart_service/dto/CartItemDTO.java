package com.smatech.cart_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private String productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double subtotal;
}