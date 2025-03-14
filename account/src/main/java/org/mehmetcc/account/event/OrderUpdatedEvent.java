package org.mehmetcc.account.event;

import org.mehmetcc.account.model.Order;

public class OrderUpdatedEvent extends OrderEvent {
    public OrderUpdatedEvent(final Order order) {
        super(order);
    }
}