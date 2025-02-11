package com.smatech.cart_service.service;

import com.smatech.cart_service.dto.*;
import com.smatech.cart_service.enums.Status;
import com.smatech.cart_service.exceptions.CartItemNotFoundException;
import com.smatech.cart_service.exceptions.CartNotFoundException;
import com.smatech.cart_service.model.Cart;
import com.smatech.cart_service.model.CartItem;
import com.smatech.cart_service.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @Override
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findById(userId)
                .orElseThrow(() -> new CartNotFoundException(userId));
        return mapToCartResponse(cart);
    }

    @KafkaListener(topics = "payment-success", groupId = "cart-service-group")
    public void handlePaymentSuccess(PaymentEvent event) {
        log.info("📌 Cart Service received Success payment event: {}", event);
        // Process the failed payment for order management
        clearCart(event.getUserId());
    }
    @Override
    public CartResponse addToCart( AddToCartRequest request) {
        Cart cart = cartRepository.findById(request.getUserId())
                .orElseGet(() -> createNewCart(request.getUserId()));


        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity if product exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());// just simply adding to the already existing
        } else {
            // Add new item if product doesn't exist

            CartItem newItem = CartItem.builder()
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
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