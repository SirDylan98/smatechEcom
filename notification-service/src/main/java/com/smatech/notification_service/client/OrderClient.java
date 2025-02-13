package com.smatech.notification_service.client;

import com.smatech.commons_library.dto.ApiResponse;
import com.smatech.commons_library.dto.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/13/2025
 */
@FeignClient(name = "order-service", url = "http://localhost:8083/api/v1/orders")
public interface OrderClient {
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable String orderId) ;
}
