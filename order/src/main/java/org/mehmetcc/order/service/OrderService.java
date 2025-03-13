package org.mehmetcc.order.service;

import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.model.OrderStatus;
import org.mehmetcc.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class OrderService {
    private final OrderRepository repository;

    @Autowired
    public OrderService(final OrderRepository repository) {
        this.repository = repository;
    }

    public Optional<String> create(final Order order) {
        try {
            Objects.requireNonNull(order);
            return Optional.of(repository.save(order).getId());
        } catch (Exception e) {
            log.error("Exception occurred during order creation: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<Order> getAll() {
        return repository.findAll();
    }

    public Boolean delete(final String orderId) {
        try {
            Objects.requireNonNull(orderId);
            var found = repository
                    .findById(orderId)
                    .filter(order -> order.getStatus() == OrderStatus.PENDING)
                    .map(order -> {
                        order.setStatus(OrderStatus.CANCELLED);
                        return order;
                    });

            if (found.isPresent()) {
                repository.save(found.get());
                return true;
            } else {
                log.error("Exception occurred during order deletion: order does not exist.");
                return false;
            }
        } catch (Exception e) {
            log.error("Exception occurred during order deletion: {}", e.getMessage());
            return false;
        }
    }
}
