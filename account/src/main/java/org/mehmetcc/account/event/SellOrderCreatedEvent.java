package org.mehmetcc.account.event;

import org.mehmetcc.account.model.Order;

public class SellOrderCreatedEvent extends OrderEvent {
    public SellOrderCreatedEvent(final Order order) {
        super(order);
    }
}