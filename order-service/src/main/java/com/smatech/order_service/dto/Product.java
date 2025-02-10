package com.smatech.order_service.dto;

import com.smatech.order_service.enums.Currency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Product {
    private String productCode;
    private String productName;
    private String ProductDescription;
    private Double productOnSalePrice;
    private String productImage;
    private Double productPrice;
    private Boolean onSale;
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.USD;
}
