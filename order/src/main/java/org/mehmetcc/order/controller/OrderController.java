package org.mehmetcc.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mehmetcc.order.dto.*;
import org.mehmetcc.order.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@Valid @RequestBody final CreateOrderRequest request) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        // limit customers
        if (!isAdmin && !request.getCustomerId().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        var created = service.create(request.toOrder());
        return created
                .map(result -> ResponseEntity.ok(new CreateOrderResponse(result)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<ListOrderResponse> all(
            @RequestParam(required = false) final String customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate endDate
    ) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        var effectiveCustomerId = customerId;

        if (!isAdmin) {
            if (customerId != null && !customerId.equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            effectiveCustomerId = auth.getName();
        }

        var orders = service.readAll(effectiveCustomerId, startDate, endDate)
                .stream()
                .map(GetOrderResponse::fromOrder)
                .toList();
        return ResponseEntity.ok(new ListOrderResponse(orders));
    }

    @DeleteMapping
    public ResponseEntity<DeleteOrderResponse> delete(@RequestParam final String orderId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        var isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        if (!isAdmin) {
            var orderOpt = service.findById(orderId); // do i still need this?
            if (orderOpt.isEmpty() || !orderOpt.get().getCustomerId().equals(auth.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(new DeleteOrderResponse(service.delete(orderId)));
    }
}
