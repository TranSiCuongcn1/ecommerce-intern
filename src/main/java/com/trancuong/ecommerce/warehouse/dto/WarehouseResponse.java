package com.trancuong.ecommerce.warehouse.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String code,
        String name,
        String address,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
