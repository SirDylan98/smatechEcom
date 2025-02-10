package com.smatech.payment_service.exception;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
public class PaymentProcessingException extends RuntimeException{
    public PaymentProcessingException(String message){
        super(message);
    }
}
