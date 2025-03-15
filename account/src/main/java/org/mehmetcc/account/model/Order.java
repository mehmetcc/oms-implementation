package org.mehmetcc.account.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
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

    // For testing purposes
    @JsonCreator
    public Order(
            @JsonProperty("id") String id,
            @JsonProperty("asset_name") String assetName,
            @JsonProperty("create_date") Long createDate,
            @JsonProperty("customer_id") String customerId,
            @JsonProperty("order_side") OrderSide orderSide,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("size") BigDecimal size,
            @JsonProperty("status") OrderStatus status) {
        this.id = id;
        this.assetName = assetName;
        this.createDate = createDate;
        this.customerId = customerId;
        this.orderSide = orderSide;
        this.price = price;
        this.size = size;
        this.status = status;
    }
}

