package com.turkcell.order_service.service;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.order_service.entity.OutboxMessage;
import com.turkcell.order_service.entity.OutboxStatus;
import com.turkcell.order_service.event.OrderCreatedEvent;
import com.turkcell.order_service.repository.OutboxMessageRepository;

@Service
public class OutboxPublisher {
    private static final String ORDER_CREATED_BINDING = "orderCreatedEvent-out-0";

    private final OutboxMessageRepository outboxMessageRepository;
    private final StreamBridge streamBridge;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(
            OutboxMessageRepository outboxMessageRepository,
            StreamBridge streamBridge,
            ObjectMapper objectMapper) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.streamBridge = streamBridge;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${app.outbox.publish-delay-ms:2000}")
    @Transactional
    public void publishPendingMessages() {
        outboxMessageRepository.findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING)
                .forEach(this::publish);
    }

    private void publish(OutboxMessage message) {
        var event = toEvent(message);
        if (streamBridge.send(ORDER_CREATED_BINDING, event)) {
            message.markPublished();
        }
    }

    private OrderCreatedEvent toEvent(OutboxMessage message) {
        try {
            return objectMapper.readValue(message.getPayload(), OrderCreatedEvent.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Outbox message could not be deserialized: " + message.getId(), exception);
        }
    }
}
