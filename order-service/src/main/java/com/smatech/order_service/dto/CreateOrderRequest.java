package com.smatech.order_service.dto;

import com.smatech.order_service.enums.Currency;
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
public class CreateOrderRequest extends BaseDto{

    private String userId;
    private Currency currency;
    private String shippingAddress;
}
