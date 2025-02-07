package com.smatech.product_service.dto;

import com.smatech.product_service.enums.Category;
import com.smatech.product_service.enums.Currency;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateProductDto extends BaseDto {

    private String productCode;
    private String productName;
    private String ProductDescription;
    @Enumerated(EnumType.STRING)
    private Category productCategory;
    private String productImage;
    private Double productPrice;
    private Boolean onSale;
    private Integer startingQuantity;
    private Integer minimumReorderLevel;
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.USD;
}
