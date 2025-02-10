package com.smatech.order_service.dto;

import com.smatech.inventory_service.enums.InventoryStatus;
import lombok.*;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InventoryItemStatus {
    private String productCode;
    private int requestedQuantity;
    private int availableQuantity;
    private InventoryStatus status;
    private String message;
}
