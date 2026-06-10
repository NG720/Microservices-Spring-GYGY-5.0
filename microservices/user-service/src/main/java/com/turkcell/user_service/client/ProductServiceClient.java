package com.turkcell.user_service.client;

// UserService içinde ProductService isteklerini atacak kişi.

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.turkcell.user_service.client.model.ProductTestResponse;

@FeignClient(name="product-service")
public interface ProductServiceClient {
    @GetMapping("/api/products/test")
    ProductTestResponse test();
}