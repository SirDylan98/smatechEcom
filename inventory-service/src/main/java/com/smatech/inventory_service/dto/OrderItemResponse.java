package com.smatech.inventory_service.dto;

import lombok.*;

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
public class OrderItemResponse {
    private String productId;
    private Integer quantity;
    private Double price;
    private Boolean onSale;
    private Double salePrice;
}
