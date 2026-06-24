package com.trancuong.ecommerce.inventory.dto;

import java.util.UUID;

public record InventoryAllocationResponse(
        UUID inventoryId,
        ProductSummary product,
        WarehouseSummary warehouse,
        Integer allocatedQuantity,
        Integer quantityOnHand,
        Integer quantityReserved,
        Integer availableQuantity
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
