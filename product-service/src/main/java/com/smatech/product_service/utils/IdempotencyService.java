package com.smatech.product_service.utils;

import com.smatech.product_service.dto.IdempotencyBody;
import com.smatech.product_service.enums.IdempotencyStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
public class IdempotencyService {
    public  static Map<String, IdempotencyBody> orderServiceIdomMap= new HashMap<>();

    public static void setProductServiceIdomMap(String idemKey, IdempotencyBody idempotencyBody){
        orderServiceIdomMap.put(idemKey,idempotencyBody);
    }
    public static  IdempotencyBody getProductServiceIdemp(String idemKey){
        return orderServiceIdomMap.getOrDefault(idemKey,IdempotencyBody.builder()
                .requestStatus(IdempotencyStatus.NOT_FOUND.name())
                .build());

    }
}
