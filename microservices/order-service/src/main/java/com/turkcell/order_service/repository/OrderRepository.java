package com.turkcell.order_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.order_service.entity.CustomerOrder;

public interface OrderRepository extends JpaRepository<CustomerOrder, UUID> {
    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);
}
