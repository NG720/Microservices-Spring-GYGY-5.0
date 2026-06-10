package com.turkcell.order_service.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(List<OrderItemRequest> products) {

    public record OrderItemRequest(UUID productId, int quantity) {
    }
}
