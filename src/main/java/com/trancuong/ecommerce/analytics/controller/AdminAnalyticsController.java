package com.trancuong.ecommerce.analytics.controller;

import com.trancuong.ecommerce.analytics.dto.DashboardSummaryResponse;
import com.trancuong.ecommerce.analytics.dto.TopCustomerResponse;
import com.trancuong.ecommerce.analytics.dto.TopSellingProductResponse;
import com.trancuong.ecommerce.analytics.service.AnalyticsService;
import com.trancuong.ecommerce.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Analytics", description = "Admin Dashboard & Revenue Reporting APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get admin dashboard high-level metrics & status breakdown")
    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> getDashboardSummary() {
        return ApiResponse.success(analyticsService.getDashboardSummary());
    }

    @Operation(summary = "Get top selling products by quantity & revenue")
    @GetMapping("/top-products")
    public ApiResponse<List<TopSellingProductResponse>> getTopSellingProducts(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.success(analyticsService.getTopSellingProducts(limit));
    }

    @Operation(summary = "Get top spending customers")
    @GetMapping("/top-customers")
    public ApiResponse<List<TopCustomerResponse>> getTopCustomers(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.success(analyticsService.getTopCustomers(limit));
    }
}
