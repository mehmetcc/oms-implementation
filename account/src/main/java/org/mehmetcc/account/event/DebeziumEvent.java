package org.mehmetcc.account.event;

import lombok.Data;
import org.mehmetcc.account.model.Order;

@Data
public class DebeziumEvent {
    private String op;
    private Order after;
}