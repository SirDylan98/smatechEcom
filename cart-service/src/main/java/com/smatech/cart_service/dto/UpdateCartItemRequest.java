package com.smatech.cart_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class UpdateCartItemRequest {
    @NotNull
    @Min(0)
    private Integer quantity;
}