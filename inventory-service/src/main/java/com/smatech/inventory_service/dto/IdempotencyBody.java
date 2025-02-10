package com.smatech.inventory_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 7/27/2024
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdempotencyBody {
    private String idemKey;
    private String requestBody;
    private String responseBody;
    private String requestStatus;
    private LocalDateTime requestDateTime;
}
