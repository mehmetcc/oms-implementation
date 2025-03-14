package org.mehmetcc.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mehmetcc.account.event.DebeziumEvent;
import org.mehmetcc.account.event.OrderCreatedEvent;
import org.mehmetcc.account.event.OrderEvent;
import org.mehmetcc.account.event.OrderUpdatedEvent;
import org.springframework.stereotype.Component;

@Component
public class OrderEventParser {
    private final ObjectMapper objectMapper;

    public OrderEventParser(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public OrderEvent parseEvent(String eventJson) throws Exception {
        DebeziumEvent event = objectMapper.readValue(eventJson, DebeziumEvent.class);

        return switch (event.getOp().toLowerCase()) {
            case "c" -> new OrderCreatedEvent(event.getAfter());
            case "u" -> new OrderUpdatedEvent(event.getAfter());
            default -> throw new IllegalArgumentException("Unsupported operation: " + event.getOp());
        };
    }
}
