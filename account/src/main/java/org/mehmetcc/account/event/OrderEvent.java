package org.mehmetcc.account.event;

import lombok.Getter;
import org.mehmetcc.account.model.Order;

@Getter
public abstract class OrderEvent {
    private final Order order;

    public OrderEvent(final Order order) {
        this.order = order;
    }
}
