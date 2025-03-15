package org.mehmetcc.account.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.account.event.OrderProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class OrderProcessedEventProducer {
    private final KafkaTemplate<String, String> template;

    private final ObjectMapper mapper;

    public OrderProcessedEventProducer(final KafkaTemplate<String, String> template) {
        this.template = template;
        this.mapper = new ObjectMapper();
    }

    private static final String TOPIC = "account.order.processed";

    public OrderProcessedEvent sendOrderProcessedEvent(final OrderProcessedEvent event) {
        var maybeJson = parseJson(event);
        maybeJson.ifPresent(s -> template.send(TOPIC, event.getOrderId(), s));
        return event;
    }

    // i want to test this directly (so that if the schema changes we know where the problem is) hence it is protected, instead of private
    protected Optional<String> parseJson(final OrderProcessedEvent event) {
        try {
            var json = mapper.writeValueAsString(event);
            return Optional.of(json);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while parsing JSON: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
