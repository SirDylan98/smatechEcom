package com.smatech.cart_service.client;

import com.smatech.cart_service.utils.ApiResponse;
import com.smatech.commons_library.dto.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(name = "products-service", url = "http://localhost:8086/api/v1/products")
public interface ProductsClient {

    @GetMapping("/getProductInCode")
    public ApiResponse<List<Product>> findByProductsCode(@RequestParam Set<String> codeSet) ;
}
