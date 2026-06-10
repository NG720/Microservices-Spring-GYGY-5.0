package com.turkcell.product_service.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox")
public class OutboxEvent {
    @Id
    @Column(updatable = false)
    private UUID id;

    @Column(name = "aggregate_type")
    private String aggregateType; // Product

    @Column(name = "aggregate_id")
    private String aggregateId; // ProductId , Aggregate -> İlgili nesne

    @Column(name = "event_type")
    private String eventType; // TestEvent

    @Column
    private String topic; // Kafka topic

    @Column(columnDefinition = "TEXT")
    private String payload; // JSON

    @Column(name = "created_at")
    private Instant createdAt; // Şu tarihte sıraya aldım

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
