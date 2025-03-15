package org.mehmetcc.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mehmetcc.order.event.OrderProcessedEvent;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.model.OrderStatus;
import org.mehmetcc.order.repository.OrderRepository;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OrderProcessedEventConsumerTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private Logger log;

    private OrderProcessedEventConsumer consumer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        consumer = new OrderProcessedEventConsumer(orderRepository);
        objectMapper = new ObjectMapper();
    }

    @Test
    void testHandleValidKafkaMessage() throws Exception {
        // Arrange
        String orderId = "order1";
        OrderProcessedEvent event = new OrderProcessedEvent(orderId, OrderStatus.MATCHED);
        String message = objectMapper.writeValueAsString(event);

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(existingOrder));
        // this is a solution I did not know. thanks chatgpt
        // without this, Repository::save returns null in perpetuity
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        consumer.listen(message);

        // Assert
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order updatedOrder = captor.getValue();

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.MATCHED);
        verify(orderRepository).findById(orderId);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testHandleKafkaMessageForNonExistingOrder() throws Exception {
        // Arrange
        String orderId = "order2";
        OrderProcessedEvent event = new OrderProcessedEvent(orderId, OrderStatus.CANCELLED);
        String message = objectMapper.writeValueAsString(event);

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        consumer.listen(message);

        // Assert
        verify(orderRepository).findById(orderId);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testHandleInvalidKafkaMessage() {
        // Arrange
        String invalidMessage = "invalid_json";

        // Act
        consumer.listen(invalidMessage);

        // Assert
        verify(orderRepository, never()).findById(anyString());
        verify(orderRepository, never()).save(any(Order.class));
    }
}
