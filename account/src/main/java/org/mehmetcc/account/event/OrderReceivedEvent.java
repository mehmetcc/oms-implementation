package org.mehmetcc.account.event;

import lombok.Getter;
import org.mehmetcc.account.model.Order;

@Getter
public abstract class OrderReceivedEvent {
    private final Order order;

    public OrderReceivedEvent(final Order order) {
        this.order = order;
    }
}
