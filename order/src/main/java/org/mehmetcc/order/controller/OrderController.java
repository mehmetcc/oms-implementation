package org.mehmetcc.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mehmetcc.order.dto.*;
import org.mehmetcc.order.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@Valid @RequestBody final CreateOrderRequest request) {
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
        return ResponseEntity.ok(new ListOrderResponse(
                service.readAll(customerId, startDate, endDate)
                        .stream()
                        .map(GetOrderResponse::fromOrder)
                        .toList()
        ));
    }

    @DeleteMapping
    public ResponseEntity<DeleteOrderResponse> delete(final String orderId) {
        var isDeleted = service.delete(orderId);
        if (isDeleted) return ResponseEntity.ok(new DeleteOrderResponse("Great success!!!!!!!"));
        else return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DeleteOrderResponse("Can't delete. Please refer to the logs"));

    }
}
