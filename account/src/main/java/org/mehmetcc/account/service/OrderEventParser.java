package org.mehmetcc.account.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.account.event.BuyOrderCreatedEvent;
import org.mehmetcc.account.event.DebeziumEvent;
import org.mehmetcc.account.event.OrderEvent;
import org.mehmetcc.account.event.SellOrderCreatedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventParser {
    private final ObjectMapper objectMapper;

    public OrderEvent parse(String eventJson) throws Exception {
        DebeziumEvent event = objectMapper.readValue(eventJson, DebeziumEvent.class);

        return switch (event.getAfter().getOrderSide()) {
            case BUY -> new BuyOrderCreatedEvent(event.getAfter());
            case SELL -> new SellOrderCreatedEvent(event.getAfter());
        };
    }
}
