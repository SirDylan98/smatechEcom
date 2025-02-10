package com.smatech.order_service.dto;

import lombok.*;

import java.util.List;

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
public class InventoryCheckResult {
    private List<InventoryItemStatus> itemStatuses;
    private boolean hasOutOfStock;
    private boolean hasLowStock;
}
