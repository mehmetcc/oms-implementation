package org.mehmetcc.order.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.mehmetcc.order.model.OrderStatus;

@Data
public class OrderProcessedEvent {
    private final String orderId;
    private final OrderStatus status;

    @JsonCreator
    public OrderProcessedEvent(@JsonProperty("orderId") String orderId, @JsonProperty("status") OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
    }

    public static OrderProcessedEvent cancelled(final String orderId) {
        return new OrderProcessedEvent(orderId, OrderStatus.CANCELLED);
    }

    public static OrderProcessedEvent matched(final String orderId) {
        return new OrderProcessedEvent(orderId, OrderStatus.MATCHED);
    }
}
