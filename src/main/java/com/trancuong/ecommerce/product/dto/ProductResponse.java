package com.trancuong.ecommerce.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        CategorySummary category,
        String name,
        String slug,
        String description,
        BigDecimal price,
        String imageUrl,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record CategorySummary(
            UUID id,
            String name,
            String slug
    ) {
    }
}
