package com.trancuong.ecommerce.analytics.service;

import com.trancuong.ecommerce.analytics.dto.DashboardSummaryResponse;
import com.trancuong.ecommerce.analytics.dto.TopCustomerResponse;
import com.trancuong.ecommerce.analytics.dto.TopSellingProductResponse;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DashboardSummaryResponse getDashboardSummary() {
        BigDecimal totalRevenue = orderRepository.calculateTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        long totalOrders = orderRepository.count();
        long totalCustomers = userRepository.count();
        long totalProducts = productRepository.count();

        Map<String, Long> breakdown = new HashMap<>();
        List<Object[]> statusCounts = orderRepository.countOrdersByStatus();
        for (Object[] row : statusCounts) {
            String status = (String) row[0];
            Long count = (Long) row[1];
            if (status != null) {
                breakdown.put(status, count);
            }
        }

        return new DashboardSummaryResponse(
                totalRevenue,
                totalOrders,
                totalCustomers,
                totalProducts,
                breakdown
        );
    }

    public List<TopSellingProductResponse> getTopSellingProducts(int limit) {
        int pageSize = limit > 0 ? limit : 5;
        List<Object[]> rows = orderItemRepository.findTopSellingProducts(PageRequest.of(0, pageSize));
        return rows.stream().map(row -> new TopSellingProductResponse(
                (UUID) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                (BigDecimal) row[3]
        )).toList();
    }

    public List<TopCustomerResponse> getTopCustomers(int limit) {
        int pageSize = limit > 0 ? limit : 5;
        List<Object[]> rows = orderRepository.findTopCustomers(PageRequest.of(0, pageSize));
        return rows.stream().map(row -> new TopCustomerResponse(
                (UUID) row[0],
                (String) row[1],
                (String) row[2],
                ((Number) row[3]).longValue(),
                (BigDecimal) row[4]
        )).toList();
    }
}
