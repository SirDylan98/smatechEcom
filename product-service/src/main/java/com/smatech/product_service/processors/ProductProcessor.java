package com.smatech.product_service.processors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.smatech.product_service.dto.CreateProductDto;
import com.smatech.product_service.dto.IdempotencyBody;
import com.smatech.product_service.enums.IdempotencyStatus;
import com.smatech.product_service.model.Product;
import com.smatech.product_service.service.ProductService;
import com.smatech.product_service.utils.ApiResponse;
import com.smatech.product_service.utils.IdempotencyService;
import com.smatech.product_service.utils.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductProcessor {
    private final ProductService productService;
  public ApiResponse<Product> createProduct(CreateProductDto product) {
      // first we check for idompotency
      IdempotencyBody idempotencyBody = IdempotencyService.getProductServiceIdemp(product.toString());
      log.info("---------->This is the idempotent Body {}", JsonUtil.toJson(idempotencyBody));
      if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.NOT_FOUND.name())) {
          // means this is a new request

          Product response = productService.createProduct(product);
          IdempotencyService.setProductServiceIdomMap(product.toString(), IdempotencyBody.builder()
                  .requestStatus(response != null ? IdempotencyStatus.SUCCESS.name() : IdempotencyStatus.FAILED.name())
                  .requestBody(product.toString())
                  .responseBody(JsonUtil.toJson(response))
                  .requestDateTime(LocalDateTime.now())
                  .idemKey(product.getUniquekey())
                  .build());
          return new ApiResponse<>(response, "Product Created Successfully", HttpStatus.OK.value());
      } else {
          // handle if previous was successful
          if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.SUCCESS.name())) {
              TypeReference<Product> typeRef = new TypeReference<Product>() {};
              return new ApiResponse<>(JsonUtil.fromJson(idempotencyBody.getResponseBody(), typeRef ), "Product Created Successfully", HttpStatus.OK.value());
          } else if (idempotencyBody.getRequestStatus().equals(IdempotencyStatus.FAILED.name())) {
              return new ApiResponse<>(null, "Error in creating Product", HttpStatus.INTERNAL_SERVER_ERROR.value());

          } else {
              return new ApiResponse<>(null, "Error in creating Product", HttpStatus.INTERNAL_SERVER_ERROR.value());
          }
          // handle if previous was failure
      }

  }
}
