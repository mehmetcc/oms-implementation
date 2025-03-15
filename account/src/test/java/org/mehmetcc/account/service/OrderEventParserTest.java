package org.mehmetcc.account.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mehmetcc.account.event.BuyOrderCreatedReceivedEvent;
import org.mehmetcc.account.event.DebeziumEvent;
import org.mehmetcc.account.event.OrderReceivedEvent;
import org.mehmetcc.account.event.SellOrderCreatedReceivedEvent;
import org.mehmetcc.account.model.Order;
import org.mehmetcc.account.model.OrderSide;
import org.mehmetcc.account.model.OrderStatus;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderEventParserTest {
    private OrderEventParser orderEventParser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        orderEventParser = new OrderEventParser(objectMapper);
    }

    @Test
    void testParseBuyOrderEvent() throws Exception {
        // Arrange
        Order order = createSampleOrder(OrderSide.BUY);
        DebeziumEvent debeziumEvent = createDebeziumEvent(order);
        String eventJson = objectMapper.writeValueAsString(debeziumEvent);

        // Act
        OrderReceivedEvent result = orderEventParser.parse(eventJson);

        // Assert
        assertThat(result).isInstanceOf(BuyOrderCreatedReceivedEvent.class);
        assertThat(result.getOrder()).isEqualTo(order);
    }

    @Test
    void testParseSellOrderEvent() throws Exception {
        // Arrange
        Order order = createSampleOrder(OrderSide.SELL);
        DebeziumEvent debeziumEvent = createDebeziumEvent(order);
        String eventJson = objectMapper.writeValueAsString(debeziumEvent);

        // Act
        OrderReceivedEvent result = orderEventParser.parse(eventJson);

        // Assert
        assertThat(result).isInstanceOf(SellOrderCreatedReceivedEvent.class);
        assertThat(result.getOrder()).isEqualTo(order);
    }

    @Test
    void testParseInvalidJsonThrowsException() {
        // Arrange
        String invalidJson = "invalid_json";

        // Act & Assert
        assertThatThrownBy(() -> orderEventParser.parse(invalidJson))
                .isInstanceOf(JsonProcessingException.class);
    }

    private Order createSampleOrder(OrderSide side) {
        return Order.builder()
                .id("order1")
                .customerId("customer123")
                .assetName("AAPL")
                .orderSide(side)
                .size(BigDecimal.valueOf(10))
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.PENDING)
                .createDate(System.currentTimeMillis())
                .build();
    }

    private DebeziumEvent createDebeziumEvent(Order order) {
        DebeziumEvent event = new DebeziumEvent();
        event.setOp("c");
        event.setAfter(order);
        return event;
    }
}
