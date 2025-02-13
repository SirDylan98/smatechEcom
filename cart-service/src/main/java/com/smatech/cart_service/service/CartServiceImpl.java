package com.smatech.cart_service.service;

import com.smatech.cart_service.client.ProductsClient;
import com.smatech.cart_service.dto.*;
import com.smatech.cart_service.enums.Status;
import com.smatech.cart_service.exceptions.CartItemNotFoundException;
import com.smatech.cart_service.exceptions.CartNotFoundException;
import com.smatech.cart_service.model.Cart;
import com.smatech.cart_service.model.CartItem;
import com.smatech.cart_service.repository.CartRepository;
import com.smatech.cart_service.utils.ApiResponse;
import com.smatech.cart_service.utils.JsonUtil;
import com.smatech.commons_library.dto.PaymentEvent;
import com.smatech.commons_library.dto.Product;
import com.smatech.commons_library.dto.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductsClient productsClient;

    @Override
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        Set<String> codeSet = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            codeSet.add(cartItem.getProductId());
        }
        ApiResponse<List<Product>> allProducts = productsClient.findByProductsCode(codeSet);
        if (allProducts.getStatusCode() != 200) {
            throw new RuntimeException("Failed to retrieve products from Product Service");
        }
        List<Product> productList = allProducts.getBody(); // Get the product list

        // Map product codes to actual products for easy lookup
        Map<String, Product> productMap = productList.stream()
                .collect(Collectors.toMap(Product::getProductCode, product -> product));

        double totalCartPrice = 0.0; // Initialize cart price

        // Iterate over cart items, find the corresponding product, and update price
        List<CartItemDTO> cartItemDTOList = new ArrayList<CartItemDTO>();

        for (CartItem cartItem : cart.getItems().stream().filter(cartItemDTO -> cartItemDTO.getStatus().equals(Status.ACTIVE)).collect(Collectors.toList())) {
            Product product = productMap.get(cartItem.getProductId());
            if (product == null) {
                throw new RuntimeException("Product not found for Cart Item: " + cartItem.getProductId());
            }
            CartItemDTO cartItemDTO = CartItemDTO.builder()
                    .productName(product.getProductName())
                    .price(product.getOnSale() ? product.getProductOnSalePrice() : product.getProductPrice())
                    .quantity(cartItem.getQuantity())
                    .productImage(product.getProductImage())
                    .productId(cartItem.getProductId())
                    .build();
            cartItemDTOList.add(cartItemDTO);

        }
        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(cartItemDTOList)
                .lastModifiedDate(cart.getLastModifiedDate())
                .build();




    }

    @KafkaListener(topics = Topics.PAYMENT_SUCCESS_TOPIC, groupId = "cart-service-group")
    public void handlePaymentSuccess(PaymentEvent event, Acknowledgment acknowledgment) {
        log.info("=============================> Cart Service received Success payment event: {}", event);
        // Process the failed payment for order management
        clearCart(event.getUserId());
        acknowledgment.acknowledge();
    }

    @Override
    public CartResponse addToCart(AddToCartRequest request) {
        Cart cart = cartRepository.getCartByIdWithActiveItems(request.getUserId())
                .orElseGet(() -> createNewCart(request.getUserId()));
        log.info("============> This is the cart state  {}", JsonUtil.toJson(cart));


        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if product exists
            CartItem item = existingItem.get();

            item.setQuantity(item.getQuantity() + request.getQuantity());// just simply adding to the already existing
            item.setProductName(request.getProductName());
        } else {
            // Add new item if product doesn't exist

            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .quantity(request.getQuantity())
                    .status(Status.ACTIVE)
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setLastModifiedDate(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public CartResponse updateCartItemQuantity(String userId, String productId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(productId, userId));

        if (request.getQuantity() == 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(request.getQuantity());
        }

        cart.setLastModifiedDate(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override

    public CartResponse removeFromCart(String userId, String productId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new CartItemNotFoundException(productId, userId);
        }

        cart.setLastModifiedDate(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);
        return mapToCartResponse(updatedCart);
    }

    @Override
    public CartResponse subtractFromCart(SubtractFromCartRequest request) {
        Cart cart = cartRepository.findById(request.getUserId())
                .orElseThrow(() -> new CartNotFoundException(request.getUserId()));

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElseThrow(() -> new CartItemNotFoundException(request.getProductId(), request.getUserId()));

        int newQuantity = cartItem.getQuantity() - request.getQuantity();

        if (newQuantity == 0) {
            // totaly eliminate the item completely if quantity becomes 0
            cart.getItems().remove(cartItem);
        } else {
            cartItem.setQuantity(newQuantity);
        }

        cart.setLastModifiedDate(LocalDateTime.now());
        Cart updatedCart = cartRepository.save(cart);

        return mapToCartResponse(updatedCart);
    }

    @Override

    public void clearCart(String userId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));

        cart.getItems().stream().forEach(cartItem -> cartItem.setStatus(Status.PROCESSED));
        cart.setLastModifiedDate(LocalDateTime.now());
        cartRepository.save(cart);
        log.info("========================> Cart for user {} has been cleared", userId);
    }

    @Override
    public CartResponse createCartIfNotExists(String userId) {
        return cartRepository.findById(userId)
                .map(this::mapToCartResponse)
                .orElseGet(() -> mapToCartResponse(createNewCart(userId)));
    }

    private Cart createNewCart(String userId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .lastModifiedDate(LocalDateTime.now())
                .build();
        return cartRepository.save(cart);
    }

    private CartResponse mapToCartResponse(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream().filter(item -> item.getStatus().equals(Status.ACTIVE))// we makes sure kuti we only get the active ones
                .map(item -> CartItemDTO.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(itemDTOs)
                .lastModifiedDate(cart.getLastModifiedDate())
                .build();
    }
}