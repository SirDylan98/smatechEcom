package com.smatech.payment_service.model;

import com.smatech.commons_library.dto.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String orderId;
    private BigDecimal amount;
    private String userId;
    @Column(columnDefinition = "TEXT")
    private String sessionId;
    private String currency;
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String paymentIntentId;
    @Column(columnDefinition = "TEXT")
    private String clientSecret;
    @Column(columnDefinition = "TEXT")
    private String checkoutUrl;
    @Column(columnDefinition = "TEXT")
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
