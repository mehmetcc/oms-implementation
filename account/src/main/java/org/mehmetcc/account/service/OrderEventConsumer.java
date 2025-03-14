package org.mehmetcc.account.service;

import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.account.event.OrderEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class OrderEventConsumer {
    private final OrderEventParser parser;

    @Autowired
    public OrderEventConsumer(final OrderEventParser parser) {
        this.parser = parser;
    }

    // TODO: maybe change this magic string over here to a configuration
    @KafkaListener(topics = "orderdb.public.orders")
    public void listen(String message) {
        log.info("Received Kafka message: {}", message);

        try {
            OrderEvent orderEvent = parser.parseEvent(message);
            // Process the event here, e.g. save to a database or update order status.
            log.info("Processed order event: Order ID {} with status {}",
                    orderEvent.getOrder().getId(),
                    orderEvent.getOrder().getStatus());
        } catch (Exception e) {
            // There should be a DLQ, although I am too lazy to implement it atm
            log.error("Failed to process Kafka message: {}", message, e);
            // Optionally, handle the error, e.g. send the message to a dead-letter queue.
        }
    }
}
