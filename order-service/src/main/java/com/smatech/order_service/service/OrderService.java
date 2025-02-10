package com.smatech.order_service.service;

import com.smatech.order_service.dto.CreateOrderRequest;
import com.smatech.order_service.dto.OrderResponse;
import com.smatech.order_service.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(CreateOrderRequest request);
    OrderResponse getOrder(String orderId);
    List<OrderResponse> getUserOrders(String userId);
    void handlePaymentFailure(String orderId);
    void updateOrderStatus(String orderId, OrderStatus status);
}
