package com.trancuong.ecommerce.order.repository;

import com.trancuong.ecommerce.order.domain.Order;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByUserId(UUID userId, Pageable pageable);

    Optional<Order> findByIdAndUserId(UUID id, UUID userId);

    Page<Order> findAllBy(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.paymentStatus = 'PAID' OR o.status = 'COMPLETED'")
    java.math.BigDecimal calculateTotalRevenue();

    @org.springframework.data.jpa.repository.Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    java.util.List<Object[]> countOrdersByStatus();

    @org.springframework.data.jpa.repository.Query("SELECT o.user.id, o.user.fullName, o.user.email, COUNT(o), SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED' GROUP BY o.user.id, o.user.fullName, o.user.email ORDER BY SUM(o.totalAmount) DESC")
    java.util.List<Object[]> findTopCustomers(Pageable pageable);
}
