package com.smatech.order_service.utils;

import com.smatech.order_service.dto.IdempotencyBody;
import com.smatech.order_service.enums.IdempotencyStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
public class IdempotencyService {
    public  static Map<String, IdempotencyBody> inventoryServiceIdomMap= new HashMap<>();

    public static void setOrderServiceIdomMap(String idemKey, IdempotencyBody idempotencyBody){
        inventoryServiceIdomMap.put(idemKey,idempotencyBody);
    }
    public static  IdempotencyBody getOrderServiceIdemp(String idemKey){
        return inventoryServiceIdomMap.getOrDefault(idemKey,IdempotencyBody.builder()
                .requestStatus(IdempotencyStatus.NOT_FOUND.name())
                .build());

    }
}
