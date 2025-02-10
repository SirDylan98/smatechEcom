package com.smatech.product_service.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Getter
@Setter

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private String productCode;
    private int availableQuantity;
    private int reservedQuantity;
    private int restockLevel;
    private LocalDateTime lastUpdateOnRestock;
}
