package com.turkcell.product_service.controller;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.turkcell.product_service.entity.TestClass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turkcell.product_service.entity.OutboxEvent;
import com.turkcell.product_service.entity.Product;
import com.turkcell.product_service.event.TestEvent;
import com.turkcell.product_service.repository.OutboxRepository;
import com.turkcell.product_service.repository.ProductRepository;

@RequestMapping("/api/products")
@RestController
public class ProductsController {
    private final ProductRepository productRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public ProductsController(
            ProductRepository productRepository,
            OutboxRepository outboxRepository,
            ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/test")
    public TestClass test2() {
        return new TestClass("Product Service Test Başarılı");
    }

    @GetMapping
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getStockQuantity()))
                .toList();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProductResponse> create(@RequestBody CreateProductRequest request) {
        validate(request);

        var product = productRepository.save(new Product(request.name().trim(), request.stockQuantity()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ProductResponse(product.getId(), product.getName(), product.getStockQuantity()));
    }

    @GetMapping(params = "message")
    public String testWithGet(@RequestParam String message) {
        return queueTestEvent(message);
    }

    @PostMapping(params = "message")
    public String test(@RequestParam String message) {
        return queueTestEvent(message);
    }

    private String queueTestEvent(String message) {
        UUID id = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        var event = new TestEvent(eventId, message, id);

        // Kafka'ya doğrudan publish edilmiyor; önce outbox'a yazılıyor.
        // Debezium PostgreSQL WAL kaydını okuyup payload'ı test-topic'e taşır.

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setId(eventId);
        outboxEvent.setAggregateType("Product");
        outboxEvent.setAggregateId(id.toString());
        outboxEvent.setEventType("TestEvent");
        outboxEvent.setTopic("test-topic");
        outboxEvent.setPayload(toJson(event));
        outboxEvent.setCreatedAt(Instant.now());

        outboxRepository.save(outboxEvent);
        System.out.println("Outbox kaydı oluşturuldu. Topic: " + outboxEvent.getTopic() + ", Event ID: " + eventId);

        return "Başarılı";
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validate(CreateProductRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product request is required");
        }
        if (request.id() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be provided");
        }
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product name is required");
        }
        if (request.stockQuantity() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stock quantity must be zero or greater");
        }
    }

    public record CreateProductRequest(UUID id, String name, int stockQuantity) {
    }

    public record ProductResponse(UUID id, String name, int stockQuantity) {
    }
}
