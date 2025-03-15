package org.mehmetcc.account.event;

import lombok.Data;
import org.mehmetcc.account.model.OrderStatus;

@Data
public class OrderProcessedEvent {
    private final String orderId;
    private final OrderStatus status;

    public static OrderProcessedEvent cancelled(final String orderId) {
        return new OrderProcessedEvent(orderId, OrderStatus.CANCELLED);
    }

    public static OrderProcessedEvent matched(final String orderId) {
        return new OrderProcessedEvent(orderId, OrderStatus.MATCHED);
    }
}
