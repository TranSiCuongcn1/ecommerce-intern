package com.trancuong.ecommerce.analytics.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardSummaryResponse(
        BigDecimal totalRevenue,
        long totalOrders,
        long totalCustomers,
        long totalProducts,
        Map<String, Long> orderStatusBreakdown
) {
}
