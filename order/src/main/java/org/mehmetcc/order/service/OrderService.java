package org.mehmetcc.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mehmetcc.order.model.Order;
import org.mehmetcc.order.model.OrderStatus;
import org.mehmetcc.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository repository;

    public Optional<String> create(final Order order) {
        try {
            Objects.requireNonNull(order);
            return Optional.of(repository.save(order).getId());
        } catch (Exception e) {
            log.error("Exception occurred during order creation: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public List<Order> readAll(final String customerId, final LocalDate startDate, final LocalDate endDate) {
        return repository.findAll().stream()
                .filter(order -> customerId == null || order.getCustomerId().equals(customerId))
                .filter(order -> startDate == null || !order.getCreateDate().isBefore(startDate.atStartOfDay()))
                .filter(order -> endDate == null || !order.getCreateDate().isAfter(endDate.atStartOfDay()))
                .collect(Collectors.toList());
    }

    public Optional<Order> findById(final String orderId) {
        return repository.findById(orderId);
    }

    public Boolean delete(final String orderId) {
        try {
            Objects.requireNonNull(orderId);
            var found = repository.findById(orderId)
                    .filter(order -> order.getStatus() == OrderStatus.PENDING)
                    .map(order -> {
                        order.setStatus(OrderStatus.CANCELLED);
                        return order;
                    });
            if (found.isPresent()) {
                repository.save(found.get());
                return true;
            } else {
                log.error("Order deletion failed: order does not exist.");
                return false;
            }
        } catch (Exception e) {
            log.error("Exception during order deletion: {}", e.getMessage());
            return false;
        }
    }
}
