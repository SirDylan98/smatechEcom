package com.smatech.product_service.utils;

import lombok.*;

//@Data
/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse <E>{
    private E body;
    private String message;
    private int statusCode;



    public ApiResponse(String message,int statusCode){
        this.statusCode=statusCode;
        this.message = message;
    }

}
