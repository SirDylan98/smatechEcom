package com.smatech.product_service.service;

import com.smatech.product_service.dto.CreateProductDto;
import com.smatech.product_service.model.Product;


import java.util.List;
import java.util.Set;

public interface ProductService {
    Product createProduct(CreateProductDto createProductDto);
    Product findByProductCode(String code);
    List< Product> findByProductInCodes(Set<String> codeSet);
    Product updateProduct(CreateProductDto createProductDto);
    void deleteProduct(String code);
    List<Product> findAllProducts();
    List<Product> findProductsByCategory(String category);
    List<Product> findProductsByPriceRange(double minPrice, double maxPrice);
    List<Product> findProductsOnSale();
    Product putProductOnSale(CreateProductDto createProductDto);
    List<Product> searchForProduct(String searchKey);
}
