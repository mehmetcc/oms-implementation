package org.mehmetcc.account.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mehmetcc.account.event.OrderProcessedEvent;
import org.mehmetcc.account.model.OrderStatus;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderProcessedEventProducerTest {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private OrderProcessedEventProducer producer;
    private ObjectMapper objectMapper;

    private static final String TOPIC = "account.order.processed";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producer = new OrderProcessedEventProducer(kafkaTemplate);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testSendOrderProcessedEventSuccessfully() throws JsonProcessingException {
        // Arrange
        OrderProcessedEvent event = new OrderProcessedEvent("order1", OrderStatus.MATCHED);
        String expectedJson = objectMapper.writeValueAsString(event);

        // Act
        producer.sendOrderProcessedEvent(event);

        // Assert
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(eq(TOPIC), keyCaptor.capture(), valueCaptor.capture());
        assertThat(keyCaptor.getValue()).isEqualTo("order1");
        assertThat(valueCaptor.getValue()).isEqualTo(expectedJson);
    }

    @Test
    void testSendOrderProcessedEventWithJsonProcessingFailure() {
        // Arrange
        OrderProcessedEvent event = new OrderProcessedEvent("order1", OrderStatus.MATCHED);
        OrderProcessedEventProducer spyProducer = spy(producer);

        doReturn(Optional.empty()).when(spyProducer).parseJson(any());

        // Act
        spyProducer.sendOrderProcessedEvent(event);

        // Assert
        verify(kafkaTemplate, never()).send(anyString(), anyString(), anyString());
    }

    @Test
    void testParseJsonSuccessfully() throws JsonProcessingException {
        // Arrange
        OrderProcessedEvent event = new OrderProcessedEvent("order1", OrderStatus.MATCHED);
        String expectedJson = objectMapper.writeValueAsString(event);

        // Act
        Optional<String> json = producer.parseJson(event);

        // Assert
        assertThat(json).isPresent().contains(expectedJson);
    }

    @Test
    void testParseJsonFailure() {
        // Arrange
        OrderProcessedEvent event = mock(OrderProcessedEvent.class);
        when(event.getOrderId()).thenThrow(new RuntimeException("Serialization error"));

        // Act
        Optional<String> json = producer.parseJson(event);

        // Assert
        assertThat(json).isEmpty();
    }
}
