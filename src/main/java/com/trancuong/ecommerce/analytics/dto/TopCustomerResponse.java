package com.trancuong.ecommerce.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TopCustomerResponse(
        UUID userId,
        String fullName,
        String email,
        long totalOrders,
        BigDecimal totalSpent
) {
}
