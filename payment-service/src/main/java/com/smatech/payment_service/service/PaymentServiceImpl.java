package com.smatech.payment_service.service;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */


import com.smatech.payment_service.dto.OrderEvent;
import com.smatech.payment_service.dto.PaymentEvent;
import com.smatech.payment_service.enums.PaymentStatus;
import com.smatech.payment_service.exception.PaymentProcessingException;
import com.smatech.payment_service.model.Payment;
import com.smatech.payment_service.repository.PaymentRepository;
import com.smatech.payment_service.utils.JsonUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${application.frontend.payment-success-url}")
    private String successUrl;

    @Value("${application.frontend.payment-cancel-url}")
    private String cancelUrl;

    @Override
    public Payment processPaymentRequest(OrderEvent orderEvent) {
        log.info("Received payment request for order: {}", JsonUtil.toJson(orderEvent));

        try {

            Payment payment = initializePaymentRecord(orderEvent);

            // Create Stripe checkout session
            Session checkoutSession = createStripeCheckoutSession(payment);

            // Update payment record with Stripe details
            payment.setSessionId(checkoutSession.getId());
            payment.setCheckoutUrl(checkoutSession.getUrl());
            paymentRepository.save(payment);

            // Send payment initiated event with checkout URL
            return  payment;


        } catch (Exception e) {
            log.error("Failed to process payment request", e);
            handlePaymentFailure(orderEvent, e);
            return null;
        }
    }
    private Session createStripeCheckoutSession(Payment payment) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8085/api/v1" + "/webhook/stripe/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:8085/api/v1" + "/webhook/stripe/cancel?session_id={CHECKOUT_SESSION_ID}")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(payment.getCurrency().toLowerCase())
                                                .setUnitAmount(payment.getAmount().longValue() * 100L) // Convert to cents
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Order #" + payment.getOrderId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("orderId", payment.getOrderId())
                .build();

        return Session.create(params);
    }
    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        try {
            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
            log.info("<========================> Yes sir  tawinner1 ==================================> {}",event.toString());

            switch (event.getType()) {
                case "checkout.session.completed":
                    //log.info("<========================> Yes sir  tawinner2 ==================================> {}",JsonUtil.toJson(event));
                    handlePaymentSuccess(event);
                    break;

                case "checkout.session.expired":
                    handlePaymentFailure(event);
                    break;

                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("==================> Error processing webhook", e);
            throw new PaymentProcessingException("Webhook processing failed "+e.getMessage());
        }
    }

    private Payment initializePaymentRecord(OrderEvent orderEvent) {
        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .userId(orderEvent.getUserId())
                .amount(orderEvent.getAmount())
                .currency(orderEvent.getCurrency())
                .status(PaymentStatus.CREATED)
                .createdAt(LocalDateTime.now())
                .build();

        return payment;
    }

//    private PaymentIntent createStripePaymentIntent(Payment payment) throws StripeException, StripeException {
//        Stripe.apiKey = stripeSecretKey;
//
//        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
//                .setAmount(payment.getAmount().longValue() * 100) // Convert to cents
//                .setCurrency(payment.getCurrency().toLowerCase())
//                .setAutomaticPaymentMethods(
//                        PaymentIntentCreateParams.AutomaticPaymentMethods
//                                .builder()
//                                .setEnabled(true)
//                                .build()
//                )
//                .putMetadata("orderId", payment.getOrderId())
//                .setReceiptEmail(payment.getCustomerEmail())
//                .build();
//
//        return PaymentIntent.create(params);
//    }


    private void handlePaymentSuccess(Event event) {

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new IllegalStateException("Invalid event data")
        );

        if (stripeObject instanceof Session) {
            Session session = (Session) stripeObject;


            String orderId = session.getMetadata().get("orderId");

            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalStateException("Order ID is missing in metadata");
            }

            log.info("✅ Payment successful for Order ID: {}", orderId);
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found for session: " + orderId));


            payment.setStatus(PaymentStatus.COMPLETED);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Send success event
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(payment.getOrderId())
                    .status(PaymentStatus.COMPLETED)
                    .amount(payment.getAmount())
                    .timestamp(LocalDateTime.now())
                    .build();
        } else {
            throw new IllegalStateException("Unexpected event object type: " + stripeObject.getClass());
        }


        //kafkaTemplate.send("payment-success", paymentEvent);
    }

    private void handlePaymentFailure(Event event) {
        Session session = (Session) event.getDataObjectDeserializer().getObject().get();
        String sessionId = session.getId();

        log.info("<========================> Processing payment for session: {} ==================================>", sessionId);

        Payment payment = paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for session: " + sessionId));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Checkout session expired");
        paymentRepository.save(payment);

        // Create payment event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .orderId(payment.getOrderId())
                .status(PaymentStatus.FAILED)
                .errorMessage(payment.getFailureReason())
                .timestamp(LocalDateTime.now())
                .build();

        //kafkaTemplate.send("payment-failure", paymentEvent);
    }

    private void handlePaymentFailure(OrderEvent orderEvent, Exception e) {
        // Create failure record
        Payment payment = Payment.builder()
                .orderId(orderEvent.getOrderId())
                .amount(orderEvent.getAmount())
                .status(PaymentStatus.FAILED)
                .failureReason(e.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Send failure event
        PaymentEvent paymentEvent = PaymentEvent.builder()
                .orderId(orderEvent.getOrderId())
                .status(PaymentStatus.FAILED)
                .errorMessage(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

       // kafkaTemplate.send("payment-failure", paymentEvent);
    }
}