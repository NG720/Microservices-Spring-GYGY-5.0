package com.turkcell.order_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.turkcell.order_service.dto.CreateOrderRequest;
import com.turkcell.order_service.dto.OrderResponse;
import com.turkcell.order_service.service.OrderCommandService;

@RequestMapping("/api/orders")
@RestController
public class OrdersController {
    private final OrderCommandService orderCommandService;

    public OrdersController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @GetMapping
    public String get() {
        System.out.println("OrdersController çalıştı");
        return "OrdersController";
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody CreateOrderRequest request) {
        var response = orderCommandService.create(request, idempotencyKey);
        var status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
