package com.smatech.inventory_service.model;

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
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    private String productCode;
    private String productName;
    private int availableQuantity;
    private int reservedQuantity;
    private int restockLevel;
    private LocalDateTime lastUpdateOnRestock;
}
