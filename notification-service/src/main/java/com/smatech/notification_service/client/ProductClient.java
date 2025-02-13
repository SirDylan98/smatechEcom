package com.smatech.notification_service.client;

import com.smatech.commons_library.dto.ApiResponse;
import com.smatech.commons_library.dto.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/13/2025
 */
@FeignClient(name = "products-service", url = "http://localhost:8086/api/v1/products")
public interface ProductClient {
    @GetMapping("/getProductInCode")
    public ApiResponse<List<Product>> findByProductsCode(@RequestParam Set<String> codeSet);
}
