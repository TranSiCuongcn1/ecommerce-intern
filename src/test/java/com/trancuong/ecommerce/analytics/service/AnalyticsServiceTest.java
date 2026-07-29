package com.trancuong.ecommerce.analytics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.analytics.dto.DashboardSummaryResponse;
import com.trancuong.ecommerce.analytics.dto.TopCustomerResponse;
import com.trancuong.ecommerce.analytics.dto.TopSellingProductResponse;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.user.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getDashboardSummary_returnsMetricsAndBreakdown() {
        when(orderRepository.calculateTotalRevenue()).thenReturn(new BigDecimal("50000000"));
        when(orderRepository.count()).thenReturn(100L);
        when(userRepository.count()).thenReturn(50L);
        when(productRepository.count()).thenReturn(20L);

        Object[] statusPending = new Object[]{"PENDING", 10L};
        Object[] statusCompleted = new Object[]{"COMPLETED", 90L};
        when(orderRepository.countOrdersByStatus()).thenReturn(List.of(statusPending, statusCompleted));

        DashboardSummaryResponse summary = analyticsService.getDashboardSummary();

        assertThat(summary.totalRevenue()).isEqualTo(new BigDecimal("50000000"));
        assertThat(summary.totalOrders()).isEqualTo(100L);
        assertThat(summary.totalCustomers()).isEqualTo(50L);
        assertThat(summary.totalProducts()).isEqualTo(20L);
        assertThat(summary.orderStatusBreakdown()).containsEntry("COMPLETED", 90L);
    }

    @Test
    void getTopSellingProducts_returnsTopProducts() {
        UUID prodId = UUID.randomUUID();
        Object[] row = new Object[]{prodId, "iPhone 15", 15L, new BigDecimal("300000000")};
        when(orderItemRepository.findTopSellingProducts(any())).thenReturn(java.util.Collections.singletonList(row));

        List<TopSellingProductResponse> topProducts = analyticsService.getTopSellingProducts(5);

        assertThat(topProducts).hasSize(1);
        assertThat(topProducts.get(0).productName()).isEqualTo("iPhone 15");
        assertThat(topProducts.get(0).totalQuantitySold()).isEqualTo(15L);
    }

    @Test
    void getTopCustomers_returnsTopSpenders() {
        UUID userId = UUID.randomUUID();
        Object[] row = new Object[]{userId, "John Doe", "john@example.com", 5L, new BigDecimal("100000000")};
        when(orderRepository.findTopCustomers(any())).thenReturn(java.util.Collections.singletonList(row));

        List<TopCustomerResponse> topCustomers = analyticsService.getTopCustomers(5);

        assertThat(topCustomers).hasSize(1);
        assertThat(topCustomers.get(0).fullName()).isEqualTo("John Doe");
        assertThat(topCustomers.get(0).totalSpent()).isEqualTo(new BigDecimal("100000000"));
    }
}
