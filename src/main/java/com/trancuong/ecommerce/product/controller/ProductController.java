package com.trancuong.ecommerce.product.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Map<String, Object>> getProducts() {
        return List.of();
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable UUID id) {
        return Map.of("id", id, "message", "TODO: return product detail");
    }
}
