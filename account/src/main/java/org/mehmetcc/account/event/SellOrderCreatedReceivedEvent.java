package org.mehmetcc.account.event;

import org.mehmetcc.account.model.Order;

public class SellOrderCreatedReceivedEvent extends OrderReceivedEvent {
    public SellOrderCreatedReceivedEvent(final Order order) {
        super(order);
    }
}