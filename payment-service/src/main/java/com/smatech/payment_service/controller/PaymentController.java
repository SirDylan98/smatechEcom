package com.smatech.payment_service.controller;

import com.smatech.payment_service.dto.OrderEvent;
import com.smatech.payment_service.model.Payment;
import com.smatech.payment_service.service.PaymentService;
import com.smatech.payment_service.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Inventory Controller", description = "Endpoints for Inventory Management")
public class PaymentController {
    private  final PaymentService paymentService;
    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
                                                @RequestHeader("Stripe-Signature") String signature) {
        log.info("----> Incoming Stripe webhook hit {}", payload);
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok("Webhook processed");
    }
    @Operation(summary = "Process a payment request")
    @PostMapping("/process")
    public ApiResponse<String> processPayment(@RequestBody OrderEvent orderEvent) {
        log.info("----> Incoming Payment request for order {}", orderEvent.getOrderId());
        try {
            Payment payment= paymentService.processPaymentRequest(orderEvent);

            // Fetch the checkout URL from the saved payment


            return new ApiResponse<>(payment.getCheckoutUrl(), "Payment processing initiated successfully", HttpStatus.OK.value());
        } catch (Exception e) {
            log.error("Error processing payment request", e);
            return new ApiResponse<>(null, "Failed to process payment request: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

}
