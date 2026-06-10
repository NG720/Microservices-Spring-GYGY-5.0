package com.turkcell.order_service.event;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(UUID eventId, UUID orderId, Instant occurredAt, List<OrderItem> products) {

    public record OrderItem(UUID productId, int quantity) {
    }
}
