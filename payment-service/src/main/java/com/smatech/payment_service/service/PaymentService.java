package com.smatech.payment_service.service;

import com.smatech.commons_library.dto.PaymentStatus;
import com.smatech.payment_service.dto.OrderEvent;
import com.smatech.payment_service.model.Payment;
import com.stripe.model.checkout.Session;

public interface PaymentService {
    public void handleStripeWebhook(String payload, String signatureHeader);
    public Payment processPaymentRequest(OrderEvent orderEvent);
    public PaymentStatus checkPaymentStatus(String sessionId);
    public Payment getPaymentBySessionId(String sessionId);
    public Session getSessionFromPaymentIntent(String paymentIntentId);
}
