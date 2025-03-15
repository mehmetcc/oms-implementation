package org.mehmetcc.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class ListOrderResponse {
    private final List<GetOrderResponse> orders;
}