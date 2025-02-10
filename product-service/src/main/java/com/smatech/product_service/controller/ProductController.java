package com.smatech.product_service.controller;

import com.smatech.product_service.dto.CreateProductDto;
import com.smatech.product_service.model.Product;
import com.smatech.product_service.processors.ProductProcessor;
import com.smatech.product_service.service.ProductService;
import com.smatech.product_service.utils.ApiResponse;
import com.smatech.product_service.utils.JsonUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/7/2025
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Controller", description = "Endpoints for Product Management")
public class ProductController {
    private final ProductService productService;
    private final ProductProcessor productProcessor;

    @Operation(summary = "Create a new product")
    @PostMapping("/create")
    public ApiResponse<Product> createProduct(@RequestBody @Valid CreateProductDto request) {
        log.info("----> Incoming Create Product request {}", JsonUtil.toJson(request));
        ApiResponse<Product> response = productProcessor.createProduct(request);
        return response;
    }

    @Operation(summary = "Find product by code")
    @GetMapping("/{code}")
    public ApiResponse<Product> findByProductCode(@PathVariable String code) {
        log.info("----> Fetching product with code: {}", code);
        Product response = productService.findByProductCode(code);
        if (response == null) {
            return new ApiResponse<>(null, "Product not found", HttpStatus.NOT_FOUND.value());
        }
        return new ApiResponse<>(response, "Product retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Update an existing product")
    @PutMapping("/update")
    public ApiResponse<Product> updateProduct(@RequestBody @Valid CreateProductDto request) {
        log.info("----> Incoming Update Product request {}", JsonUtil.toJson(request));
        Product response = productService.updateProduct(request);
        if (response == null) {
            return new ApiResponse<>(null, "Product not found", HttpStatus.OK.value());
        }
        return new ApiResponse<>(response, "Product updated successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Delete a product by code")
    @DeleteMapping("/{code}")
    public ApiResponse<Void> deleteProduct(@PathVariable String code) {
        log.info("----> Deleting product with code: {}", code);
        productService.deleteProduct(code);
        return new ApiResponse<>(null, "Product deleted successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Retrieve all products")
    @GetMapping
    public ApiResponse<List<Product>> findAllProducts() {
        log.info("----> Fetching all products");
        List<Product> response = productService.findAllProducts();
        return new ApiResponse<>(response, "Products retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Find products by category")
    @GetMapping("/category/{category}")
    public ApiResponse<List<Product>> findProductsByCategory(@PathVariable String category) {
        log.info("----> Fetching products for category: {}", category);
        List<Product> response = productService.findProductsByCategory(category);
        return new ApiResponse<>(response, "Products retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Find products by price range")
    @GetMapping("/price-range")
    public ApiResponse<List<Product>> findProductsByPriceRange(
            @RequestParam double minPrice,
            @RequestParam double maxPrice) {
        log.info("----> Fetching products between price range: {} - {}", minPrice, maxPrice);
        List<Product> response = productService.findProductsByPriceRange(minPrice, maxPrice);
        return new ApiResponse<>(response, "Products retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Find products on sale")
    @GetMapping("/on-sale")
    public ApiResponse<List<Product>> findProductsOnSale() {
        log.info("----> Fetching products on sale");
        List<Product> response = productService.findProductsOnSale();
        return new ApiResponse<>(response, "Products on sale retrieved successfully", HttpStatus.OK.value());
    }

    @Operation(summary = "Search products by keyword")
    @GetMapping("/search")
    public ApiResponse<List<Product>> searchProducts(@RequestParam String searchKey) {
        log.info("----> Searching products with key: {}", searchKey);
        List<Product> response = productService.searchForProduct(searchKey);
        return new ApiResponse<>(response, "Search results retrieved successfully", HttpStatus.OK.value());
    }
}
