package org.mehmetcc.account.event;

import org.mehmetcc.account.model.Order;

public class OrderCreatedEvent extends OrderEvent {
    public OrderCreatedEvent(final Order order) {
        super(order);
    }
}