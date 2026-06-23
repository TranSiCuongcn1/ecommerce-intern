package com.trancuong.ecommerce.inventory.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        ProductSummary product,
        WarehouseSummary warehouse,
        Integer quantityOnHand,
        Integer quantityReserved,
        Integer availableQuantity,
        Integer reorderLevel,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public record ProductSummary(
            UUID id,
            String name,
            String slug
    ) {
    }

    public record WarehouseSummary(
            UUID id,
            String code,
            String name
    ) {
    }
}
