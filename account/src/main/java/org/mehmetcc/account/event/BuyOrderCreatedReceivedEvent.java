package org.mehmetcc.account.event;

import org.mehmetcc.account.model.Order;

public class BuyOrderCreatedReceivedEvent extends OrderReceivedEvent {
    public BuyOrderCreatedReceivedEvent(final Order order) {
        super(order);
    }
}