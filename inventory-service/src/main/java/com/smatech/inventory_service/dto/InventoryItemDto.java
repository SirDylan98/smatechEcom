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
public class InventoryItemDto {
    private String productCode;
    private int availableQuantity;
    private int reservedQuantity;
    private int restockLevel;
}
