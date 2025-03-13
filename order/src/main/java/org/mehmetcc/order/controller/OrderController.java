package org.mehmetcc.order.controller;

import jakarta.validation.Valid;
import org.mehmetcc.order.dto.CreateOrderRequest;
import org.mehmetcc.order.dto.CreateOrderResponse;
import org.mehmetcc.order.dto.DeleteOrderResponse;
import org.mehmetcc.order.dto.ListOrderResponse;
import org.mehmetcc.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService service;

    @Autowired
    public OrderController(final OrderService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@Valid @RequestBody final CreateOrderRequest request) {
        var created = service.create(request.toOrder());
        return created
                .map(result -> ResponseEntity.ok(new CreateOrderResponse(result)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping
    public ResponseEntity<ListOrderResponse> all() {
        return ResponseEntity.ok(new ListOrderResponse(service
                .getAll()
                .stream()
                .map(current -> new CreateOrderResponse(current.getId())).toList()));
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
