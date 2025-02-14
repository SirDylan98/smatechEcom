package com.smatech.product_service.service;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/10/2025
 */


import com.smatech.product_service.Exceptions.DuplicateResourceException;
import com.smatech.product_service.client.InventoryClient;
import com.smatech.product_service.dto.CreateProductDto;
import com.smatech.product_service.dto.Inventory;
import com.smatech.product_service.enums.Category;
import com.smatech.product_service.model.Product;
import com.smatech.product_service.repository.ProductRepository;
import com.smatech.product_service.utils.UtilsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryClient inventoryClient;
    private static final String PRODUCTS = "products";

    @Override
    public Product createProduct(CreateProductDto createProductDto) {
        log.info("Creating new product with code: {}", createProductDto.getProductCode());
        createProductDto.setProductCode(UtilsService.generateProductCode("P"));
        // Check if product already exists
        if (productRepository.existsByProductCode(createProductDto.getProductCode())) {
            throw new DuplicateResourceException("Product already exists with code: " + createProductDto.getProductCode());
        }


        Product product = new Product();
        mapDtoToProduct(createProductDto, product);


        Product savedProduct = productRepository.save(product);

        // Initialize inventory if starting quantity is provided
        if (createProductDto.getStartingQuantity() != null) {
            inventoryClient.initializeInventory(Inventory.builder()
                    .productCode(savedProduct.getProductCode())
                            .productName(savedProduct.getProductName())
                    .availableQuantity(createProductDto.getStartingQuantity())
                    .reservedQuantity(0)
                    .restockLevel(createProductDto.getMinimumReorderLevel())
                    .build());
        }

        return savedProduct;
    }

    @Override
//    @Cacheable(value = PRODUCTS,key = "#productCode")
    public Product findByProductCode(String code) {
        log.info("Finding product with code: {}", code);
        return productRepository.findByProductCode(code)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with code: " + code));
    }

    @Override
    public List<Product> findByProductInCodes(Set<String> codeSet) {
        return productRepository.findByProductCodeIn(codeSet);
    }

    @Override
//    @CachePut(value = PRODUCTS,key = "#createProductDto.productCode")
    public Product updateProduct(CreateProductDto createProductDto) {
        log.info("Updating product with code: {}", createProductDto.getProductCode());

        Product existingProduct = findByProductCode(createProductDto.getProductCode());
        existingProduct.setProductName(createProductDto.getProductName().isEmpty()?existingProduct.getProductName() : createProductDto.getProductName());
        existingProduct.setProductDescription(createProductDto.getProductDescription().isEmpty()? existingProduct.getProductDescription() : createProductDto.getProductDescription());
        existingProduct.setProductPrice(createProductDto.getProductPrice()==0? existingProduct.getProductPrice() : createProductDto.getProductPrice());
        existingProduct.setProductCategory(createProductDto.getProductCategory());
        existingProduct.setProductOnSalePrice(createProductDto.getProductOnSalePrice()==0? existingProduct.getProductOnSalePrice() : createProductDto.getProductOnSalePrice());
        mapDtoToProduct(createProductDto, existingProduct);

        return productRepository.save(existingProduct);
    }

    @Override
    @CacheEvict(value = PRODUCTS,key = "productCode")
    public void deleteProduct(String code) {
        log.info("Deleting product with code: {}", code);

        if (!productRepository.existsByProductCode(code)) {
            throw new EntityNotFoundException("Product not found with code: " + code);
        }

        // Delete associated inventory first
        inventoryClient.deleteInventory(code);

        productRepository.deleteByProductCode(code);
    }

    @Override

    public List<Product> findAllProducts() {
        log.info("Fetching all products");
        return productRepository.findAll();
    }

    @Override
    public List<Product> findProductsByCategory(String category) {
        log.info("Finding products by category: {}", category);
        try {
            Category productCategory = Category.valueOf(category.toUpperCase());
            return productRepository.findByProductCategory(productCategory);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
    }

    @Override
    public List<Product> findProductsByPriceRange(double minPrice, double maxPrice) {
        log.info("Finding products in price range: {} - {}", minPrice, maxPrice);
        if (minPrice < 0 || maxPrice < minPrice) {
            throw new IllegalArgumentException("Invalid price range");
        }
        return productRepository.findByProductPriceBetween(minPrice, maxPrice);
    }

    @Override
    public List<Product> findProductsOnSale() {
        log.info("Finding products on sale");
        return productRepository.findByOnSaleTrue();
    }

    @Override
    public Product putProductOnSale(CreateProductDto createProductDto) {
        log.info("Putting product on sale with code: {}", createProductDto.getProductCode());
        Product existingProduct = findByProductCode(createProductDto.getProductCode());
        existingProduct.setOnSale(true);
        existingProduct.setProductOnSalePrice(createProductDto.getProductOnSalePrice());
        return productRepository.save(existingProduct);

    }

    @Override
    public List<Product> searchForProduct(String searchKey) {
        log.info("Searching for products with key: {}", searchKey);
        if (searchKey == null || searchKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Search key cannot be empty");
        }

        return productRepository.searchProducts(searchKey);
    }

    private void mapDtoToProduct(CreateProductDto dto, Product product) {
        product.setProductCode(dto.getProductCode());
        product.setProductName(dto.getProductName());
        product.setProductDescription(dto.getProductDescription());
        product.setProductCategory(dto.getProductCategory());
        product.setProductImage(dto.getProductImage());
        product.setProductPrice(dto.getProductPrice());
        product.setOnSale(dto.getOnSale());
        product.setCurrency(dto.getCurrency());

        // Set sale price to regular price if not on sale
        if (Boolean.TRUE.equals(dto.getOnSale())) {
            product.setProductOnSalePrice(dto.getProductPrice());
        } else {
            product.setProductOnSalePrice(null);
        }
    }
}
