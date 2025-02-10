package com.smatech.payment_service.service;

import com.smatech.payment_service.dto.OrderEvent;
import com.smatech.payment_service.model.Payment;

public interface PaymentService {
    public void handleStripeWebhook(String payload, String signatureHeader);
    public Payment processPaymentRequest(OrderEvent orderEvent);
}
