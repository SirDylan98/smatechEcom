package com.smatech.order_service.clients;

import com.smatech.order_service.dto.OrderEventDetails;
import com.smatech.order_service.event.OrderCreatedEvent;
import com.smatech.order_service.utils.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
@FeignClient(name = "payment-service", url = "http://localhost:8085/api/v1/payments")
public interface PaymentServiceClient {
    @PostMapping("/process")
    public ApiResponse<String> processPayment(@RequestBody OrderEventDetails orderEvent) ;
}
