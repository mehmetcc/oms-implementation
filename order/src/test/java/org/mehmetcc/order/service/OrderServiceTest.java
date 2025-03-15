package org.mehmetcc.order.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.model.OrderSide;
import org.mehmetcc.order.model.OrderStatus;
import org.mehmetcc.order.repository.OrderRepository;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository);
    }

    @Test
    void testCreateOrderSuccess() {
        // Arrange
        Order order = createSampleOrder(null, OrderStatus.PENDING);
        Order savedOrder = createSampleOrder("order1", OrderStatus.PENDING);
        when(orderRepository.save(order)).thenReturn(savedOrder);

        // Act
        Optional<String> result = orderService.create(order);

        // Assert
        assertThat(result).isPresent().contains("order1");
        verify(orderRepository).save(order);
    }

    @Test
    void testCreateOrderException() {
        // Arrange
        Order order = createSampleOrder(null, OrderStatus.PENDING);
        when(orderRepository.save(order)).thenThrow(new RuntimeException("Database error"));

        // Act
        Optional<String> result = orderService.create(order);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository).save(order);
    }

    @Test
    void testReadAllOrders() {
        // Arrange
        Order order1 = createSampleOrder("order1", OrderStatus.PENDING, "customer1");
        Order order2 = createSampleOrder("order2", OrderStatus.PENDING, "customer2");
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // Act
        List<Order> orders = orderService.readAll(null, null, null);

        // Assert
        assertThat(orders).hasSize(2).containsExactly(order1, order2);
        verify(orderRepository).findAll();
    }

    @Test
    void testDeleteOrderSuccess() {
        // Arrange
        Order order = createSampleOrder("order1", OrderStatus.PENDING);
        when(orderRepository.findById("order1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // Act
        String result = orderService.delete("order1");

        // Assert
        assertThat(result).isEqualTo("Order successfully cancelled");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).findById("order1");
        verify(orderRepository).save(order);
    }

    @Test
    void testDeleteOrderNotFound() {
        // Arrange
        when(orderRepository.findById("order1")).thenReturn(Optional.empty());

        // Act
        String result = orderService.delete("order1");

        // Assert
        assertThat(result).isEqualTo("Failed to fetch the order. Please consult logs for details");
        verify(orderRepository).findById("order1");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testDeleteOrderNonPending() {
        // Arrange
        Order order = createSampleOrder("order1", OrderStatus.MATCHED);
        when(orderRepository.findById("order1")).thenReturn(Optional.of(order));

        // Act
        String result = orderService.delete("order1");

        // Assert
        assertThat(result).isEqualTo("Order already matched");
        verify(orderRepository).findById("order1");
        verify(orderRepository, never()).save(any(Order.class));
    }

    private Order createSampleOrder(String id, OrderStatus status) {
        return createSampleOrder(id, status, "customer1");
    }

    private Order createSampleOrder(String id, OrderStatus status, String customerId) {
        return Order.builder()
                .id(id)
                .customerId(customerId)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal(10))
                .price(BigDecimal.valueOf(150))
                .status(status)
                .createDate(LocalDateTime.now())
                .build();
    }
}
