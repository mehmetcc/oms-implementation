package org.mehmetcc.account.event;

import org.mehmetcc.account.model.Order;

public class BuyOrderCreatedEvent extends OrderEvent {
    public BuyOrderCreatedEvent(final Order order) {
        super(order);
    }
}