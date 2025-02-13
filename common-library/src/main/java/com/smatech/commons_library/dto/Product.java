package com.smatech.commons_library.dto;


import lombok.Getter;
import lombok.Setter;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Getter
@Setter

public class Product {

    private String productCode;
    private String productName;
    private String productDescription;
    private Category productCategory;
    private String productImage;
    private Double productPrice;
    private Boolean onSale;
    private Double productOnSalePrice;

    private Currency currency = Currency.USD;


}
