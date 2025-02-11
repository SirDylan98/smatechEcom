package com.smatech.inventory_service.client;

import com.smatech.inventory_service.dto.OrderResponse;
import com.smatech.inventory_service.utils.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/11/2025
 */
@FeignClient(name = "order-service", url = "http://localhost:8083/api/v1/orders")
public interface OrderServiceClient {

    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String orderId);
}
