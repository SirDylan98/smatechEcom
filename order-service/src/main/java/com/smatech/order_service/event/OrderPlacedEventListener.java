package com.smatech.order_service.event;
import com.smatech.order_service.utils.JsonUtil;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPlacedEventListener {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    //private final ObservationRegistry observationRegistry;

    @EventListener
    public void handleOrderPlacedEvent(OrderCreatedEvent event) {
        log.info("Order Placed Event Received, Sending OrderPlacedEvent to order-created-topic: {}", JsonUtil.toJson( event.getOrderEventDetails()));

//        // Create Observation for Kafka Template
//        try {
//            Observation.createNotStarted("order-created-topic", this.observationRegistry).observeChecked(() -> {
//                CompletableFuture<SendResult<String, OrderCreatedEvent>> future = kafkaTemplate.send("order-created-topic", event);
//                return future.handle((result, throwable) -> CompletableFuture.completedFuture(result));
//            }).get();
//        } catch (InterruptedException | ExecutionException e) {
//            Thread.currentThread().interrupt();
//            throw new RuntimeException("Error while sending message to Kafka", e);
//        }
    }
}
