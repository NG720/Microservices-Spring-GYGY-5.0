package com.turkcell.order_service.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.turkcell.order_service.entity.CustomerOrder;
import com.turkcell.order_service.entity.OrderLine;

public record OrderResponse(
        UUID orderId,
        String status,
        String idempotencyKey,
        boolean created,
        Instant createdAt,
        List<OrderItemResponse> products) {

    public static OrderResponse from(CustomerOrder order, boolean created) {
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getIdempotencyKey(),
                created,
                order.getCreatedAt(),
                order.getLines().stream().map(OrderItemResponse::from).toList());
    }

    public record OrderItemResponse(UUID productId, int quantity) {

        static OrderItemResponse from(OrderLine line) {
            return new OrderItemResponse(line.getProductId(), line.getQuantity());
        }
    }
}
