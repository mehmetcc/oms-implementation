package org.mehmetcc.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.model.OrderSide;
import org.mehmetcc.order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateOrderRequest {
    @NotBlank
    private final String customerId;

    @NotBlank
    private final String assetName;

    @NotNull
    private final OrderSide orderSide;

    @NotNull
    @Positive
    private final BigDecimal size;

    @NotNull
    @Positive
    private final BigDecimal price;

    public Order toOrder() {
        return Order.builder()
                .customerId(getCustomerId())
                .assetName(getAssetName())
                .orderSide(getOrderSide())
                .size(getSize())
                .price(getPrice())
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();
    }
}
