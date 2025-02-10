package com.smatech.inventory_service.dto;

import lombok.*;
import org.springframework.stereotype.Service;

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
public class InventoryDto extends BaseDto {
    private List<InventoryItemDto> inventoryItemDtos;

}
