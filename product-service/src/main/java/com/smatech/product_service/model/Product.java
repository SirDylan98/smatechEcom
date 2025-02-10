package com.smatech.product_service.model;

import com.smatech.product_service.enums.Category;
import com.smatech.product_service.enums.Currency;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Getter
@Setter
@Entity
public class Product {
    @Id
    private String productCode;
    private String productName;
    private String ProductDescription;
    @Enumerated(EnumType.STRING)
    private Category productCategory;
    private String productImage;
    private Double productPrice;
    private Boolean onSale;
    private Double productOnSalePrice;
    @Enumerated(EnumType.STRING)
    private Currency currency = Currency.USD;


}
