package com.smatech.payment_service.repository;

import com.smatech.payment_service.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,String> {
    Optional<Payment> findByOrderId(String orderId);
    Optional<Payment> findBySessionId(String sessionId);
}
