package com.smatech.cart_service.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */

@Slf4j
public  class UtilsService<T> {

    public static<T>  String logData(T body){
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            return  objectMapper.writeValueAsString(body);
        }catch (Exception e){
            log.error("FAILED TO LOG DATA {}",e.getMessage());
            return e.getMessage();
        }
    }
    public static String generateProductCode(String prefix){
        return new StringBuilder().append(prefix).append(" ").append(prefix + UUID.randomUUID().toString().substring(0,5)).toString();

    }
}
