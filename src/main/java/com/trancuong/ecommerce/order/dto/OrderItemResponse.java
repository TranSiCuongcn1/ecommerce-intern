package com.trancuong.ecommerce.order.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        ProductSummary product,
        WarehouseSummary warehouse,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal subtotal
) {

    public record ProductSummary(UUID id, String name, String slug) {
    }

    public record WarehouseSummary(UUID id, String code, String name) {
    }
}
