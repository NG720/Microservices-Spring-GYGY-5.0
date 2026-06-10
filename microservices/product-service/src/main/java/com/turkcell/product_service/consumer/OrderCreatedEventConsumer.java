package com.turkcell.product_service.consumer;

import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.turkcell.product_service.event.OrderCreatedEvent;
import com.turkcell.product_service.service.ProductStockService;

@Configuration
public class OrderCreatedEventConsumer {

    @Bean
    public Consumer<OrderCreatedEvent> processOrderCreatedEvent(ProductStockService productStockService) {
        return productStockService::process;
    }
}
