package com.trancuong.ecommerce.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopSellingProductResponse(
        UUID productId,
        String productName,
        long totalQuantitySold,
        BigDecimal totalRevenue
) {
}
