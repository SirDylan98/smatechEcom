package com.smatech.order_service.event;

import com.smatech.order_service.dto.OrderEventDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
/**
 * Created by DylanDzvene
 * Email: dylandzvenetashinga@gmail.com
 * Created on: 2/9/2025
 */

@Getter
@Setter
public class OrderCreatedEvent extends ApplicationEvent {
    private OrderEventDetails orderEventDetails;

    public OrderCreatedEvent(Object source, OrderEventDetails orderEventDetails) {
        super(source);
        this.orderEventDetails = orderEventDetails;
    }

    public OrderCreatedEvent(OrderEventDetails orderEventDetails) {
        super(orderEventDetails);
        this.orderEventDetails = orderEventDetails;
    }
}
