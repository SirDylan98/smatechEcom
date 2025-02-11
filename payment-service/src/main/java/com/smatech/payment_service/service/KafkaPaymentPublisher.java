package com.smatech.payment_service.service;

import com.smatech.commons_library.dto.PaymentEvent;
import com.smatech.payment_service.exception.KafkaMessagePublishException;
import com.smatech.payment_service.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/11/2025
 */

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaPaymentPublisher {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public static final String PAYMENT_SUCCESS_TOPIC = "payment-success2";
    public static final String PAYMENT_FAILURE_TOPIC = "payment-failure2";
    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000; // 1 second


    public void publishPaymentEventSync(PaymentEvent event, String topic) {
        log.info("=======> Message sent successfully event: {}",JsonUtil.toJson(event));
        int retryCount = 0;
        while (retryCount < MAX_RETRIES) {
            try {
                SendResult<String, PaymentEvent> result = kafkaTemplate.send(topic, event).get(5, TimeUnit.SECONDS);
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("=======> Message sent successfully to topic: {}, partition: {}, offset: {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset());
                return; // Success - exit method
            } catch (Exception e) {
                retryCount++;
                log.warn("Attempt {} failed to send message to topic {}: {}",
                        retryCount, topic, e.getMessage());

                if (retryCount >= MAX_RETRIES) {
                    log.error("Failed to send message after {} attempts", MAX_RETRIES);
                    throw new KafkaMessagePublishException("Failed to publish payment event after retries", e);
                }

                try {
                    Thread.sleep(RETRY_DELAY_MS * retryCount); // Exponential backoff
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new KafkaMessagePublishException("Retry interrupted", ie);
                }
            }
        }
    }

//
}
