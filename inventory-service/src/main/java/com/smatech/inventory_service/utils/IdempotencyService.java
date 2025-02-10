package com.smatech.inventory_service.utils;

import com.smatech.inventory_service.dto.IdempotencyBody;
import com.smatech.inventory_service.enums.IdempotencyStatus;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
public class IdempotencyService {
    public  static Map<String, IdempotencyBody> inventoryServiceIdomMap= new HashMap<>();

    public static void setInventoryServiceIdomMap(String idemKey, IdempotencyBody idempotencyBody){
        inventoryServiceIdomMap.put(idemKey,idempotencyBody);
    }
    public static  IdempotencyBody getInventoryServiceIdemp(String idemKey){
        return inventoryServiceIdomMap.getOrDefault(idemKey,IdempotencyBody.builder()
                .requestStatus(IdempotencyStatus.NOT_FOUND.name())
                .build());

    }
}
