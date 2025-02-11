package com.smatech.order_service.clients;

import com.smatech.order_service.dto.Product;
import com.smatech.order_service.utils.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "http://localhost:8086/api/v1/products")
public interface ProductServiceClient {
    @GetMapping("/{code}")
    public ApiResponse<Product> findByProductCode(@PathVariable String code);
}
