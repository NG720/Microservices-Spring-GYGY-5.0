package com.turkcell.order_service.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.order_service.dto.CreateOrderRequest;
import com.turkcell.order_service.dto.OrderResponse;
import com.turkcell.order_service.entity.CustomerOrder;
import com.turkcell.order_service.entity.OutboxMessage;
import com.turkcell.order_service.event.OrderCreatedEvent;
import com.turkcell.order_service.repository.OrderRepository;
import com.turkcell.order_service.repository.OutboxMessageRepository;

@Service
public class OrderCommandService {
    private final OrderRepository orderRepository;
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public OrderCommandService(
            OrderRepository orderRepository,
            OutboxMessageRepository outboxMessageRepository,
            ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.outboxMessageRepository = outboxMessageRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request, String idempotencyKey) {
        var normalizedKey = normalizeIdempotencyKey(idempotencyKey);

        return orderRepository.findByIdempotencyKey(normalizedKey)
                .map(order -> OrderResponse.from(order, false))
                .orElseGet(() -> createOrderWithOutbox(request, normalizedKey));
    }

    private OrderResponse createOrderWithOutbox(CreateOrderRequest request, String idempotencyKey) {
        validate(request);

        var order = new CustomerOrder(UUID.randomUUID(), idempotencyKey);
        request.products().forEach(item -> order.addLine(item.productId(), item.quantity()));
        orderRepository.save(order);

        var event = new OrderCreatedEvent(
                UUID.randomUUID(),
                order.getId(),
                Instant.now(),
                request.products().stream()
                        .map(item -> new OrderCreatedEvent.OrderItem(item.productId(), item.quantity()))
                        .toList());

        outboxMessageRepository.save(new OutboxMessage(
                event.eventId(),
                "ORDER",
                order.getId(),
                "OrderCreatedEvent",
                toJson(event)));

        return OrderResponse.from(order, true);
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key header is required");
        }
        return idempotencyKey.trim();
    }

    private void validate(CreateOrderRequest request) {
        if (request == null || request.products() == null || request.products().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one product");
        }

        request.products().forEach(item -> {
            if (item.productId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id is required");
            }
            if (item.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be greater than zero");
            }
        });
    }

    private String toJson(OrderCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Order event could not be serialized", exception);
        }
    }
}
