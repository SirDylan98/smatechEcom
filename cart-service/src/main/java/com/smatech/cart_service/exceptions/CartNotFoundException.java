package com.smatech.cart_service.exceptions;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
public class CartNotFoundException extends RuntimeException{
    public CartNotFoundException(String message){
        super(message);
    }
}
