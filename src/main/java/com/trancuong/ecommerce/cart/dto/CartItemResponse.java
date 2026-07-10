package com.trancuong.ecommerce.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        ProductSummary product,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record ProductSummary(
            UUID id,
            String name,
            String slug,
            String imageUrl,
            String status
    ) {
    }
}
