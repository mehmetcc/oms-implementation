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

public class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    private OrderService orderService;

    @BeforeEach
    void setup() {
        // pfff. java really shows its age
        MockitoAnnotations.openMocks(this);
        orderService = new OrderService(orderRepository);
    }

    @Test
    void testCreateOrderSuccess() {
        // Arrange
        Order order = Order.builder()
                .customerId("customer1")
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        Order savedOrder = Order.builder()
                .id("order1")
                .customerId("customer1")
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.PENDING)
                .createDate(order.getCreateDate())
                .build();

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
        Order order = Order.builder()
                .customerId("customer1")
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.save(order)).thenThrow(new RuntimeException("Database error"));

        // Act
        Optional<String> result = orderService.create(order);

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository).save(order);
    }

    @Test
    void testGetAllOrders() {
        // Arrange
        Order order1 = Order.builder()
                .id("order1")
                .customerId("customer1")
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        Order order2 = Order.builder()
                .id("order2")
                .customerId("customer2")
                .assetName("GOOG")
                .orderSide(OrderSide.SELL)
                .size(5)
                .price(BigDecimal.valueOf(200))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findAll()).thenReturn(List.of(order1, order2));

        // Act
        List<Order> orders = orderService.getAll();

        // Assert
        assertThat(orders).hasSize(2).containsExactly(order1, order2);
        verify(orderRepository).findAll();
    }

    @Test
    void testDeleteOrderSuccess() {
        // Arrange
        Order order = Order.builder()
                .id("order1")
                .customerId("customer1")
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findById("order1")).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        // Act
        Boolean result = orderService.delete("order1");

        // Assert
        assertThat(result).isTrue();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(orderRepository).findById("order1");
        verify(orderRepository).save(order);
    }

    @Test
    void testDeleteOrderNotFound() {
        // Arrange
        when(orderRepository.findById("order1")).thenReturn(Optional.empty());

        // Act
        Boolean result = orderService.delete("order1");

        // Assert
        assertThat(result).isFalse();
        verify(orderRepository).findById("order1");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testDeleteOrderNonPending() {
        // Arrange
        Order order = Order.builder()
                .id("order1")
                .customerId("customer1")
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(10)
                .price(BigDecimal.valueOf(150))
                .status(OrderStatus.MATCHED)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findById("order1")).thenReturn(Optional.of(order));

        // Act
        Boolean result = orderService.delete("order1");

        // Assert
        assertThat(result).isFalse();
        verify(orderRepository).findById("order1");
        verify(orderRepository, never()).save(any(Order.class));
    }
}
