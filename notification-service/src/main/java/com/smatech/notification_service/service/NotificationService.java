package com.smatech.notification_service.service;

import com.smatech.commons_library.dto.*;
import com.smatech.notification_service.client.OrderClient;
import com.smatech.notification_service.client.ProductClient;
import com.smatech.notification_service.exceptions.NotificationProcessingException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/13/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ProductClient productClient;
    private final OrderClient orderClient;
    private final EmailSenderService emailSenderService;
//    private final ApplicationEventPublisher applicationEventPublisher;
    @KafkaListener(topics = Topics.PAYMENT_FAILURE_TOPIC, groupId = "notification-service-group")
    public void handlePaymentFailure(PaymentEvent event, Acknowledgment acknowledgment) throws MessagingException {

        log.info("=========================> Order Service received failed payment event: {}", event);
        // Process the failed payment for order management
        handlePaymentFailureProcessing(event);
        acknowledgment.acknowledge();
    }
    @KafkaListener(topics = Topics.PAYMENT_SUCCESS_TOPIC, groupId = "notification-service-group")
    public void handlePaymentSuccess(PaymentEvent event, Acknowledgment acknowledgment) throws MessagingException {

        log.info("========================> Order Service received Success payment event: {}", event);
        // Process the failed payment for order management

        handlePaymentSuccessProcessing(event.getOrderId());
        acknowledgment.acknowledge();
    }
    public void handlePaymentFailureProcessing( PaymentEvent eventPay ) throws MessagingException {
        ApiResponse<OrderResponse> orderResponse = orderClient.getOrder(eventPay.getOrderId());
        if(orderResponse.getStatusCode()!=200){
            throw new NotificationProcessingException("Error in retrieving order Details");

        }

        OrderResponse order = orderResponse.getBody();
        order.setCheckoutUrl(eventPay.getErrorMessage());
        emailSenderService.sendEmailOnFailurePayment(order);
//        OrderFailedEvent event = new OrderFailedEvent (order);
//        applicationEventPublisher.publishEvent(event);



    };

    public void handlePaymentSuccessProcessing( String getOrderId) throws MessagingException {
        ApiResponse<OrderResponse> orderResponse = orderClient.getOrder(getOrderId);
        if(orderResponse.getStatusCode()!=200){
            throw new NotificationProcessingException("Error in retrieving order Details");

        }
        OrderResponse order = orderResponse.getBody();
        Set<String> productCodeSet = new HashSet<>();

       List<OrderItemResponse>  orderItems = orderResponse.getBody().getOrderItems();
       for(OrderItemResponse orderItemResponse : orderItems){
           productCodeSet.add(orderItemResponse.getProductId());
       }

       ApiResponse<List<Product>> productsResponse = productClient.findByProductsCode(productCodeSet);
       if(productsResponse.getStatusCode()!=200){
           throw new NotificationProcessingException("Error in retrieving product Details");

       }
       List<Product> productList = productsResponse.getBody();
        Map<String, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductCode, product -> product));

        for(OrderItemResponse item:orderItems){
            Product product = productMap.get(item.getProductId());
            // Send notification to customer and other relevant parties
            item.setProductName(product.getProductName());
            item.setPrice(product.getProductPrice());
            //...
        }
        order.setOrderItems(orderItems);
        emailSenderService.sendEmailOnSuccessfulPayment(order);
//        OrderCompletedEvent event = new OrderCompletedEvent(order);
//
//    applicationEventPublisher.publishEvent(event);




    };



}
