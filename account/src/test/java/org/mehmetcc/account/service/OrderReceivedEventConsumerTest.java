package org.mehmetcc.account.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mehmetcc.account.event.BuyOrderCreatedReceivedEvent;
import org.mehmetcc.account.event.OrderReceivedEvent;
import org.mehmetcc.account.event.SellOrderCreatedReceivedEvent;
import org.mehmetcc.account.model.Order;
import org.mehmetcc.account.model.OrderSide;
import org.mehmetcc.account.model.OrderStatus;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class OrderReceivedEventConsumerTest {
    @Mock
    private OrderEventParser parser;

    @Mock
    private AssetService assetService;

    private OrderReceivedEventConsumer consumer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new OrderReceivedEventConsumer(parser, assetService);
    }

    @Test
    void testListenProcessesBuyOrderEvent() throws Exception {
        // Arrange
        String message = "{\"after\":{\"id\":\"order1\",\"customer_id\":\"customer123\",\"order_side\":\"BUY\"}}";
        Order order = createSampleOrder(OrderSide.BUY);
        OrderReceivedEvent event = new BuyOrderCreatedReceivedEvent(order);

        when(parser.parse(message)).thenReturn(event);

        // Act
        consumer.listen(message);

        // Assert
        verify(parser).parse(message);
        verify(assetService).process(event);
    }

    @Test
    void testListenProcessesSellOrderEvent() throws Exception {
        // Arrange
        String message = "{\"after\":{\"id\":\"order2\",\"customer_id\":\"customer456\",\"order_side\":\"SELL\"}}"; // oh god pls work
        Order order = createSampleOrder(OrderSide.SELL);
        OrderReceivedEvent event = new SellOrderCreatedReceivedEvent(order);

        when(parser.parse(message)).thenReturn(event);

        // Act
        consumer.listen(message);

        // Assert
        verify(parser).parse(message);
        verify(assetService).process(event);
    }

    @Test
    void testListenHandlesParsingException() throws Exception {
        // Arrange
        String message = "invalid_json";
        when(parser.parse(anyString())).thenThrow(new RuntimeException("Parsing error"));

        // Act
        consumer.listen(message);

        // Assert
        verify(parser).parse(message);
        verify(assetService, never()).process(any(OrderReceivedEvent.class));
    }

    @Test
    void testListenHandlesUnexpectedException() throws Exception {
        // Arrange
        String message = "{\"after\":{\"id\":\"order1\",\"customer_id\":\"customer123\",\"order_side\":\"BUY\"}}";
        when(parser.parse(message)).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        consumer.listen(message);

        // Assert
        verify(parser).parse(message);
        verify(assetService, never()).process(any(OrderReceivedEvent.class));
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
}
