package com.smatech.inventory_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Getter
@Setter
@Entity
public class Inventory {
    @Id
    private String productCode;
    private int quantity;
    private int restockLevel;
    private LocalDateTime lastUpdateOnRestock;
}
