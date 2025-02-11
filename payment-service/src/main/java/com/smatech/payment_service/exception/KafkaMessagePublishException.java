package com.smatech.payment_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/11/2025
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class KafkaMessagePublishException extends RuntimeException {
    public KafkaMessagePublishException(String message, Throwable cause) {
        super(message, cause);
    }
}