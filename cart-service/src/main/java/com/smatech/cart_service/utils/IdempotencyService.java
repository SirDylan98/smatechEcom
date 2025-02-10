package com.smatech.cart_service.utils;

import com.smatech.cart_service.dto.IdempotencyBody;
import com.smatech.cart_service.enums.IdempotencyStatus;
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
    public  static Map<String, IdempotencyBody> cartServiceIdomMap= new HashMap<>();

    public static void setCartServiceIdomMap(String idemKey, IdempotencyBody idempotencyBody){
        cartServiceIdomMap.put(idemKey,idempotencyBody);
    }
    public static  IdempotencyBody getCartServiceIdemp(String idemKey){
        return cartServiceIdomMap.getOrDefault(idemKey,IdempotencyBody.builder()
                .requestStatus(IdempotencyStatus.NOT_FOUND.name())
                .build());

    }
}
