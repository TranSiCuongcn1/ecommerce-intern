package com.trancuong.ecommerce.inventory.service;

import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.OrderItem;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockReservationCleanupScheduler {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockReservationService stockReservationService;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Order> expiredOrders = orderRepository.findAll().stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()))
                .filter(o -> "UNPAID".equalsIgnoreCase(o.getPaymentStatus()))
                .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isBefore(cutoff))
                .toList();

        if (expiredOrders.isEmpty()) {
            return;
        }

        log.info("Found {} expired unpaid orders. Releasing reservations...", expiredOrders.size());

        for (Order order : expiredOrders) {
            List<OrderItem> items = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId());
            stockReservationService.releaseReservation(order, items);
            order.updateStatus("CANCELLED");
            log.info("Auto-cancelled expired unpaid order {}", order.getId());
        }

        orderRepository.flush();
    }
}
