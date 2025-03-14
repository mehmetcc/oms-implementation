package org.mehmetcc.account.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Order {
    public enum OrderStatus {
        PENDING, MATCHED, CANCELLED
    }

    public enum OrderSide {
        BUY, SELL
    }

    private String id;

    @JsonProperty("asset_name")
    private String assetName;

    @JsonProperty("create_date")
    private long createDate;

    @JsonProperty("customer_id")
    private String customerId;

    @JsonProperty("order_side")
    private OrderSide orderSide;

    private String price;

    private long size;

    private OrderStatus status;
}

