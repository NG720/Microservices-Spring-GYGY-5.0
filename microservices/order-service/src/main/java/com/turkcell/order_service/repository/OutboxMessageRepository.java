package com.turkcell.order_service.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.order_service.entity.OutboxMessage;
import com.turkcell.order_service.entity.OutboxStatus;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {
    List<OutboxMessage> findTop20ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
