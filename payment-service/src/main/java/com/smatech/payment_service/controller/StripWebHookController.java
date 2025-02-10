package com.smatech.payment_service.controller;

import com.smatech.payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@RestController
@RequestMapping("/api/v1/webhook/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripWebHookController {

    private final PaymentService paymentService;

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@RequestParam("session_id") String sessionId) {
        log.info("✅ Payment success callback received for session ID: {}", sessionId);

        try {
            // Process the successful payment
            //paymentService.handleCheckoutSuccess(sessionId);
            log.info("Webhook successful handler received for session ID: {}", sessionId);

            // Redirect user to a success page (frontend URL)
            return ResponseEntity.ok("Payment successful! You may now close this page.");
        } catch (Exception e) {
            log.error("⚠️ Error processing Stripe success callback", e);
            return ResponseEntity.internalServerError().body("Payment processing failed.");
        }
    }
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signatureHeader) {
        log.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Received Stripe webhook<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

        try {
            paymentService.handleStripeWebhook(payload, signatureHeader);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing Stripe webhook", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook processing failed");
        }
    }

}
