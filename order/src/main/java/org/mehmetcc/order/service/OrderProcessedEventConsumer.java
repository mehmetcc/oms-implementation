package org.mehmetcc.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.order.event.OrderProcessedEvent;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.repository.OrderRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class OrderProcessedEventConsumer {
    private final ObjectMapper mapper;

    private final OrderRepository repository;

    public OrderProcessedEventConsumer(final OrderRepository repository) {
        this.mapper = new ObjectMapper();
        this.repository = repository;
    }

    @KafkaListener(topics = "account.order.processed")
    public void listen(String message) {
        log.info("Received Kafka message: {}", message);
        try {
            var event = mapper.readValue(message, OrderProcessedEvent.class);
            if (handleOrder(event).isEmpty()) log.error("Error during unmarshalling the OrderProcessedEvent.");
            log.info("OrderProcessedEvent consumed for order id: {}", event.getOrderId());
        } catch (Exception e) {
            // There should be a DLQ, although I am too lazy to implement it atm
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }

    @Transactional
    private Optional<Order> handleOrder(final OrderProcessedEvent event) {
        return repository.findById(event.getOrderId())
                .map(order -> {
                    order.setStatus(event.getStatus());
                    return order;
                }).map(repository::save);
    }
}
