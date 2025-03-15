package org.mehmetcc.account.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.account.event.OrderReceivedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReceivedEventConsumer {
    private final OrderEventParser parser;

    private final AssetService service;

    // TODO: maybe change this magic string over here to a configuration
    @KafkaListener(topics = "orderdb.public.orders")
    public void listen(String message) {
        log.info("Received Kafka message: {}", message);
        try {
            OrderReceivedEvent event = parser.parse(message);
            service.process(event);

            log.info("Processed order event: Order ID {} with status {}",
                    event.getOrder().getId(),
                    event.getOrder().getStatus());
        } catch (Exception e) {
            // There should be a DLQ, although I am too lazy to implement it atm
            log.error("Failed to process Kafka message: {}", message, e);
        }
    }
}
