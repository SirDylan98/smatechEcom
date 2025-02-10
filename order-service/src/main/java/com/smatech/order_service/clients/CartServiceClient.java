package com.smatech.order_service.clients;

import com.smatech.order_service.dto.CartResponse;
import com.smatech.order_service.utils.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cart-service", url = "http://localhost:8082/api/v1/carts")
public interface CartServiceClient {
    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getCart(@PathVariable String userId);
}
