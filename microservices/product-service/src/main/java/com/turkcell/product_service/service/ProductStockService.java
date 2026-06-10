package com.turkcell.product_service.service;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.turkcell.product_service.entity.ProcessedEvent;
import com.turkcell.product_service.event.OrderCreatedEvent;
import com.turkcell.product_service.repository.ProcessedEventRepository;
import com.turkcell.product_service.repository.ProductRepository;

@Service
public class ProductStockService {
    private final ProductRepository productRepository;
    private final ProcessedEventRepository processedEventRepository;

    public ProductStockService(
            ProductRepository productRepository,
            ProcessedEventRepository processedEventRepository) {
        this.productRepository = productRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void process(OrderCreatedEvent event) {
        if (processedEventRepository.existsById(event.eventId())) {
            System.out.println("OrderCreatedEvent daha önce işlendi: " + event.eventId());
            return;
        }

        var requestedQuantities = requestedQuantities(event);
        var products = productRepository.findAllById(requestedQuantities.keySet());

        if (products.size() != requestedQuantities.size()) {
            throw new IllegalStateException("Order contains unknown products: " + event.orderId());
        }

        products.forEach(product -> product.decreaseStock(requestedQuantities.get(product.getId())));
        processedEventRepository.save(new ProcessedEvent(event.eventId()));

        System.out.println("OrderCreatedEvent işlendi, stok güncellendi. Order ID: " + event.orderId());
    }

    private Map<UUID, Integer> requestedQuantities(OrderCreatedEvent event) {
        if (event.products() == null || event.products().isEmpty()) {
            throw new IllegalStateException("Order event must contain products");
        }

        return event.products().stream()
                .peek(item -> {
                    if (item.productId() == null || item.quantity() <= 0) {
                        throw new IllegalStateException("Invalid order item in event: " + event.eventId());
                    }
                })
                .collect(Collectors.toMap(
                        OrderCreatedEvent.OrderItem::productId,
                        OrderCreatedEvent.OrderItem::quantity,
                        Integer::sum));
    }
}
