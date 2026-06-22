package com.trancuong.ecommerce.category.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
