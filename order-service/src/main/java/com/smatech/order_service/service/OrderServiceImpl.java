package com.smatech.order_service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smatech.commons_library.dto.PaymentEvent;
import com.smatech.commons_library.dto.Topics;
import com.smatech.order_service.clients.CartServiceClient;
import com.smatech.order_service.clients.InventoryServiceClient;
import com.smatech.order_service.clients.PaymentServiceClient;
import com.smatech.order_service.clients.ProductServiceClient;
import com.smatech.order_service.dto.*;
import com.smatech.order_service.enums.Currency;
import com.smatech.order_service.enums.OrderStatus;
import com.smatech.order_service.event.OrderCreatedEvent;
import com.smatech.order_service.exception.OrderNotFoundException;
import com.smatech.order_service.exception.OrderProcessingException;
import com.smatech.order_service.model.Orders;
import com.smatech.order_service.model.OrderItem;
import com.smatech.order_service.repository.OrderRepository;
import com.smatech.order_service.utils.ApiResponse;
import com.smatech.order_service.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = Topics.PAYMENT_FAILURE_TOPIC, groupId = "order-service-group")
    public void handlePaymentFailure(PaymentEvent event, Acknowledgment acknowledgment) {

        log.info("=========================> Order Service received failed payment event: {}", event);
        // Process the failed payment for order management
        handlePaymentFailureProcessing(event.getOrderId());
        acknowledgment.acknowledge();
    }
    @KafkaListener(topics = Topics.PAYMENT_SUCCESS_TOPIC, groupId = "order-service-group")
    public void handlePaymentSuccess(PaymentEvent event, Acknowledgment acknowledgment) {

        log.info("========================> Order Service received Success payment event: {}", event);
        // Process the failed payment for order management

        handlePaymentSuccessProcessing(event.getOrderId());
        acknowledgment.acknowledge();
    }
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        Map<String, Integer> cartItemsMap = new HashMap<>();
        try {
            ApiResponse<CartResponse> cartResponseApiResponse= cartServiceClient.getCart(request.getUserId());
            if (cartResponseApiResponse.getStatusCode()!= HttpStatus.OK.value()) {
                throw new OrderProcessingException("========> Error retrieving cart items");
            }
            List<CartItemDTO> cartItems = cartResponseApiResponse.getBody().getItems();

            if (cartItems.isEmpty()) {
                throw new OrderProcessingException("=========>Cart is empty");
            }
            for (CartItemDTO cartItemDTO : cartItems) {
                cartItemsMap.put(cartItemDTO.getProductId(), cartItemDTO.getQuantity());

            }
            // now we tave checking for inventory  and reserving the inventory items

            ApiResponse<InventoryCheckResult> checkInventoryAvailability= inventoryServiceClient.checkInventoryAvailability(cartItemsMap);
            if (checkInventoryAvailability.getStatusCode()!= HttpStatus.OK.value()) {
                throw new OrderProcessingException("==========> Error checking inventory availability");
            }
            InventoryCheckResult inventoryCheckResult = checkInventoryAvailability.getBody();

            // now get the price of the inventory and calculate the price?


            // Calculate total amount and create order items
            List<OrderItem> orderItems = new ArrayList<>();
            double totalAmount = 0.0;

            for (InventoryItemStatus cartItem : inventoryCheckResult.getItemStatuses()) {
                ApiResponse<Product> productApiResponse = productServiceClient.findByProductCode(cartItem.getProductCode());
                if (productApiResponse.getStatusCode()!= HttpStatus.OK.value()) {
                    throw new OrderProcessingException("Error retrieving product details");
                }
                Product product = productApiResponse.getBody();


                double itemPrice = product.getOnSale() ? product.getProductOnSalePrice() : product.getProductPrice();
                double itemTotal = itemPrice * cartItem.getRequestedQuantity();
                totalAmount += itemTotal;

                OrderItem orderItem = OrderItem.builder()
                        .productCode(cartItem.getProductCode())
                        .quantity(cartItem.getRequestedQuantity())
                        .unitPrice(itemPrice)
                        .build();

                orderItems.add(orderItem);
            }



            Orders order = Orders.builder()
                    .orderId(UUID.randomUUID().toString())
                    .userId(request.getUserId())
                    .orderStatus(OrderStatus.CREATED)
                    .orderItems(orderItems)
                    .totalAmount(totalAmount)
                    .currency(request.getCurrency())
                    .createdDate(LocalDateTime.now())
                    .shippingAddress(request.getShippingAddress())
                    .build();

            Orders savedOrder = orderRepository.save(order);


            OrderEventDetails event = OrderEventDetails.builder()
                    .amount(BigDecimal.valueOf(totalAmount))
                    .currency(Currency.USD.name())

                    .orderId(savedOrder.getOrderId())
                    .userId(savedOrder.getUserId())
                    .build();
            ApiResponse<String> checkOutResponse = paymentServiceClient.processPayment(event);
            if (checkOutResponse.getStatusCode()!= HttpStatus.OK.value()) {
                throw new OrderProcessingException("Error processing payment");
            }
            OrderResponse response = mapToOrderResponse(savedOrder);
            response.setCheckoutUrl(checkOutResponse.getBody());
            // this when we reserve the inventory
            ApiResponse<List<Inventory>> reserveResponse = inventoryServiceClient.reserveInventory(cartItemsMap);
            if (reserveResponse.getStatusCode()!= HttpStatus.OK.value() && reserveResponse.getBody().size()!=cartItemsMap.size()) {
                throw new OrderProcessingException("==========> Error reserving inventory");
            }

            return response;
        } catch (Exception e) {
            log.error("==========> Error creating order", e);
            throw new OrderProcessingException("=============> Failed to create order"+ e.getMessage());
        }
    }

    @Override
    public OrderResponse getOrder(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handlePaymentFailureProcessing(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(OrderStatus.PAYMENT_FAILED);
        orderRepository.save(order);
    }
    public void handlePaymentSuccessProcessing(String orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(OrderStatus.COMPLETED);
        orderRepository.save(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderId, OrderStatus status) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.setOrderStatus(status);
        orderRepository.save(order);
    }

    private OrderResponse mapToOrderResponse(Orders order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productId(item.getProductCode())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus())
                .orderItems(itemResponses)
                .totalAmount(order.getTotalAmount())
                .currency(order.getCurrency())
                .createdDate(order.getCreatedDate())
                .shippingAddress(order.getShippingAddress())
                .build();
    }
}