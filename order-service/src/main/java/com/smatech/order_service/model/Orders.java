package com.smatech.order_service.model;

import com.smatech.order_service.enums.Currency;
import com.smatech.order_service.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    @Id
    private String orderId;
    private String userId;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    @OneToMany
    @JoinColumn(name = "orderId")
    private List<OrderItem> orderItems;
    private Double totalAmount;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    private LocalDateTime createdDate;
    private String shippingAddress;
}
