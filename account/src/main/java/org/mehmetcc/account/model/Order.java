package org.mehmetcc.account.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Order {
    private String id;

    @JsonProperty("asset_name")
    private String assetName;

    @JsonProperty("create_date")
    private Long createDate;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("order_side")
    private OrderSide orderSide;

    private BigDecimal price;

    private BigDecimal size;

    private OrderStatus status;

    public BigDecimal totalPrice() {
        return price.multiply(size);
    }
}

