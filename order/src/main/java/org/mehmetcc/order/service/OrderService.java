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

    public String delete(final String orderId) {
        return repository.findById(orderId)
                .map(this::processSoftDelete)
                .orElse("Failed to fetch the order. Please consult logs for details");
    }

    private String processSoftDelete(final Order order) {
        var status = order.getStatus();

        if (status == OrderStatus.PENDING) return softDeleteOrder(order);
        else if (status == OrderStatus.CANCELLED) return "Order already cancelled";
        else if (status == OrderStatus.MATCHED) return "Order already matched";
        else return "Failed to fetch the order. Please consult logs for details";
    }

    private String softDeleteOrder(final Order order) {
        order.setStatus(OrderStatus.CANCELLED);
        repository.save(order);
        return "Order cancelled successfully";
    }
}
