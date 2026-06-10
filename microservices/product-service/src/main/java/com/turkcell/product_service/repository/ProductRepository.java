package com.turkcell.product_service.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.turkcell.product_service.entity.Product;

public interface ProductRepository extends JpaRepository<Product, UUID> {
}
