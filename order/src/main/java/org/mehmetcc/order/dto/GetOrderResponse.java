package org.mehmetcc.order.dto;

import lombok.Builder;
import lombok.Data;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.model.OrderSide;
import org.mehmetcc.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class GetOrderResponse {
    private String id;
    private String customerId;
    private OrderSide orderSide;
    private BigDecimal price;
    private OrderStatus status;
    private LocalDateTime createDate;

    public static GetOrderResponse fromOrder(final Order order) {
        return GetOrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .orderSide(order.getOrderSide())
                .price(order.getPrice())
                .status(order.getStatus())
                .createDate(order.getCreateDate())
                .build();
    }
}
