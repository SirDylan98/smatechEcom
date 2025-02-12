package com.smatech.payment_service.service;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */


import com.smatech.commons_library.dto.PaymentEvent;
import com.smatech.commons_library.dto.PaymentStatus;
import com.smatech.commons_library.dto.Topics;
import com.smatech.payment_service.dto.OrderEvent;
import com.smatech.payment_service.exception.PaymentProcessingException;
import com.smatech.payment_service.model.Payment;
import com.smatech.payment_service.repository.PaymentRepository;
import com.smatech.payment_service.utils.JsonUtil;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.model.checkout.SessionCollection;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionListParams;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final KafkaPaymentPublisher kafkaPaymentPublisher;

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
        log.info("==============>Received payment request for order: {}", JsonUtil.toJson(orderEvent));

        try {

            Payment payment = initializePaymentRecord(orderEvent);

            // Create Stripe checkout session
            Session checkoutSession = createStripeCheckoutSession(payment);
            Session checkoutSession2 = Session.retrieve(checkoutSession.getId());


            System.out.println(checkoutSession);
            // Update payment record with Stripe details
            payment.setSessionId(checkoutSession.getId());
            payment.setCheckoutUrl(checkoutSession.getUrl());
            log.info(" xxxxxxxxxxxxxxxxxxxxxxxxx > This is the payment intent ID: {}", checkoutSession2.getPaymentIntent());
            payment.setPaymentIntentId(checkoutSession.getClientSecret());
            paymentRepository.save(payment);

            // Send payment initiated event with checkout URL
            return payment;


        } catch (Exception e) {
            log.error("Failed to process payment request", e);
            handlePaymentFailure(orderEvent, e);
            return null;
        }
    }

    private Session createStripeCheckoutSession(Payment payment) throws StripeException {
        Stripe.apiKey = stripeSecretKey;
//        SessionCreateParams params = SessionCreateParams.builder()
//                .setMode(SessionCreateParams.Mode.PAYMENT)
//                .setSuccessUrl("http://localhost:8085/api/v1/webhook/stripe/success?session_id={CHECKOUT_SESSION_ID}")
//                .setCancelUrl("http://localhost:8085/api/v1/webhook/stripe/cancel?session_id={CHECKOUT_SESSION_ID}")
//                .setPaymentIntentData(
//                        SessionCreateParams.PaymentIntentData.builder()
//                                .putMetadata("orderId", payment.getOrderId())
//                                .build()
//                )
//                .addLineItem(
//                        SessionCreateParams.LineItem.builder()
//                                .setPriceData(
//                                        SessionCreateParams.LineItem.PriceData.builder()
//                                                .setCurrency(payment.getCurrency().toLowerCase())
//                                                .setUnitAmount(payment.getAmount().longValue() * 100L) // Convert to cents
//                                                .setProductData(
//                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
//                                                                .setName("Order #" + payment.getOrderId())
//                                                                .build()
//                                                )
//                                                .build()
//                                )
//                                .setQuantity(1L)
//                                .build()
//                )
//                .build();
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
                .setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES).getEpochSecond())
                .build();

        return Session.create(params);
    }


    @Override
    @Transactional
    public void handleStripeWebhook(String payload, String signatureHeader) {
        try {
            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
            log.info("<========================> Yes sir  tawinner1 ==================================> {}", event.toString());

            switch (event.getType()) {
                case "checkout.session.completed":
                    //log.info("<========================> Yes sir  tawinner2 ==================================> {}",JsonUtil.toJson(event));
                    handlePaymentSuccess(event);
                    break;
//"checkout.session.expired",
                case "payment_intent.payment_failed":
                    handlePaymentFailureIntent(event);
                    break;

                default:
                    log.info("Unhandled event type: {}", event.getType());
            }
        } catch (Exception e) {
            log.error("==================> Error processing webhook", e);
            throw new PaymentProcessingException("Webhook processing failed " + e.getMessage());
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

    //
//
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
                    .userId(payment.getUserId())
                    .status(com.smatech.commons_library.dto.PaymentStatus.COMPLETED)
                    .amount(payment.getAmount())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaPaymentPublisher.publishPaymentEventSync(paymentEvent, Topics.PAYMENT_SUCCESS_TOPIC);
        } else {
            throw new IllegalStateException("Unexpected event object type: " + stripeObject.getClass());
        }


    }

    //
    private void handlePaymentFailureIntent(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new IllegalStateException("Invalid event data")
        );

        if (stripeObject instanceof PaymentIntent) {
            PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
            String intentId = paymentIntent.getId();
            log.info("====================>Payment Intent ID from failed transaction: {}", intentId);

            // Get error details from the PaymentIntent
            String failureMessage = paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getMessage() : "Payment failed";
            String failureCode = paymentIntent.getLastPaymentError() != null ?
                    paymentIntent.getLastPaymentError().getCode() : "unknown";
            Session session= getSessionFromPaymentIntent(intentId);

//            String orderId = paymentIntent.getMetadata().get("orderId");
//            if (orderId == null || orderId.isEmpty()) {
//                throw new IllegalStateException("Order ID is missing in metadata");
//            }


            Payment payment = paymentRepository.findBySessionId(session.getId())
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found for Session: " + session.getId()));

            log.info("=================>Are we even getting to this stage : {}", JsonUtil.toJson(payment));
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(failureMessage);
            payment.setPaymentIntentId(intentId); // Store the intent ID
            paymentRepository.save(payment);

            // Create payment event
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(payment.getOrderId())
                    .status(PaymentStatus.FAILED)
                    .userId(payment.getUserId())
                    .errorMessage(failureMessage)
                    //.errorCode(failureCode)  // Include error code if your PaymentEvent has this field
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaPaymentPublisher.publishPaymentEventSync(paymentEvent, Topics.PAYMENT_FAILURE_TOPIC);
        }}
    private void handlePaymentFailure(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new IllegalStateException("Invalid event data")
        );

        if (stripeObject instanceof Session) {
            Session session = (Session) stripeObject;


            String paymentIntent = session.getId();
            System.out.println("Session ID from a failed transaction: " + paymentIntent);

            String orderId = session.getMetadata().get("orderId");

            if (orderId == null || orderId.isEmpty()) {
                throw new IllegalStateException("Order ID is missing in metadata");
            }

            log.info("=================>Payment Failure for Order ID: {}", orderId);
            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found for session: " + orderId));
            log.info("=================>Are we even getting to this stage : {}", JsonUtil.toJson(payment));
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Checkout session expired");
            paymentRepository.save(payment);

            // Create payment event
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(payment.getOrderId())
                    .status(PaymentStatus.FAILED)
                    .userId(payment.getUserId())
                    .errorMessage(payment.getFailureReason())
                    .timestamp(LocalDateTime.now())
                    .build();

            kafkaPaymentPublisher.publishPaymentEventSync(paymentEvent, Topics.PAYMENT_FAILURE_TOPIC);
        }
    }
    public Session getSessionFromPaymentIntent(String paymentIntentId) {
        try {
            Stripe.apiKey = stripeSecretKey;

            // First retrieve the PaymentIntent
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Get sessions related to this PaymentIntent
            SessionListParams params = SessionListParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();

            SessionCollection sessions = Session.list(params);

            // Usually there will be one session per PaymentIntent
            if (sessions.getData().size() > 0) {
                return sessions.getData().get(0);
            } else {
                throw new EntityNotFoundException("No session found for payment intent: " + paymentIntentId);
            }

        } catch (StripeException e) {
            log.error("Error retrieving session from payment intent: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to retrieve session: " + e.getMessage());
        }
    }

    @Override
    public PaymentStatus checkPaymentStatus(String sessionId) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Session session = Session.retrieve(sessionId);
            log.info("this is the session intent: " + session.getPaymentIntent());
            Payment payment = paymentRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found for session: " + sessionId));

            // If payment is already marked as completed or failed, return that status
            if (payment.getStatus() == PaymentStatus.COMPLETED ||
                    payment.getStatus() == PaymentStatus.FAILED) {
                return payment.getStatus();
            }

            // Check the live status from Stripe
            String paymentStatus = session.getPaymentStatus();
            if ("paid".equals(paymentStatus)) {
                payment.setStatus(PaymentStatus.COMPLETED);
                paymentRepository.save(payment);
                return PaymentStatus.COMPLETED;
            } else if ("unpaid".equals(paymentStatus)) {
                return PaymentStatus.UNPAID;
            } else {
                return PaymentStatus.FAILED;
            }

        } catch (StripeException e) {
            log.error("Error checking payment status: {}", e.getMessage());
            throw new PaymentProcessingException("Failed to check payment status");
        }
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
//@Override
//@Transactional
//public void handleStripeWebhook(String payload, String signatureHeader) {
//    try {
//        Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
//        log.info("Received Stripe webhook event: {}", event.getType());
//
//        switch (event.getType()) {
//            case "checkout.session.completed":
//                handlePaymentSuccess(event);
//                break;
//
//            // Add more specific failure cases
//            case "payment_intent.payment_failed":
//                handlePaymentIntentFailure(event);
//                break;
//            case "checkout.session.expired":
//                handleSessionExpired(event);
//                break;
//            case "payment_intent.canceled":
//                //handlePaymentCanceled(event);
//                break;
//            default:
//                log.info("Unhandled event type: {}", event.getType());
//        }
//    } catch (Exception e) {
//        log.error("Error processing webhook", e);
//        throw new PaymentProcessingException("Webhook processing failed: " + e.getMessage());
////    }
//}

    private void handlePaymentIntentFailure(Event event) {
        try {
            StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                    () -> new IllegalStateException("Invalid event data")
            );

            com.stripe.model.PaymentIntent paymentIntent = (com.stripe.model.PaymentIntent) stripeObject;
            String orderId = paymentIntent.getMetadata().get("orderId");
            String failureCode = paymentIntent.getLastPaymentError().getCode();
            String failureMessage = paymentIntent.getLastPaymentError().getMessage();

            Payment payment = paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));

            // Map specific failure reasons
            String userFriendlyMessage = mapStripeErrorToUserFriendly(failureCode, failureMessage);

            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(userFriendlyMessage);
            //payment.setStripeErrorCode(failureCode);
            paymentRepository.save(payment);

            // Send failure event with detailed information
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(payment.getOrderId())
                    .status(PaymentStatus.FAILED)
                    .userId(payment.getUserId())
                    .errorMessage(userFriendlyMessage)
                    //.errorCode(failureCode)
                    .timestamp(LocalDateTime.now())
                    .amount(payment.getAmount())
                    .build();

            kafkaPaymentPublisher.publishPaymentEventSync(paymentEvent, Topics.PAYMENT_FAILURE_TOPIC);

        } catch (Exception e) {
            log.error("Error handling payment intent failure", e);
            throw new PaymentProcessingException("Failed to process payment failure: " + e.getMessage());
        }
    }

    private String mapStripeErrorToUserFriendly(String errorCode, String defaultMessage) {
        return switch (errorCode) {
            case "card_declined" -> "Your card was declined. Please try another payment method.";
            case "insufficient_funds" -> "Insufficient funds in your account. Please try another card.";
            case "expired_card" -> "Your card has expired. Please update your card information.";
            case "incorrect_cvc" -> "The security code (CVC) is incorrect. Please check and try again.";
            case "processing_error" -> "There was an error processing your card. Please try again.";
            case "invalid_card" -> "The card information provided is invalid. Please check and try again.";
            case "authentication_required" ->
                    "Your card requires authentication. Please try again and follow the authentication steps.";
            default -> defaultMessage;
        };
    }

    private void handleSessionExpired(Event event) {
        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElseThrow(
                () -> new IllegalStateException("Invalid event data")
        );

        Session session = (Session) stripeObject;
        String orderId = session.getMetadata().get("orderId");

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for order: " + orderId));

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason("Checkout session expired");
        paymentRepository.save(payment);

        PaymentEvent paymentEvent = PaymentEvent.builder()
                .orderId(payment.getOrderId())
                .status(PaymentStatus.FAILED)
                .userId(payment.getUserId())
                .errorMessage("Your payment session has expired. Please try again.")
                .timestamp(LocalDateTime.now())
                .amount(payment.getAmount())
                .build();

        kafkaPaymentPublisher.publishPaymentEventSync(paymentEvent, Topics.PAYMENT_FAILURE_TOPIC);
    }

}