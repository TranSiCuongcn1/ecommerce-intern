package com.trancuong.ecommerce.order.repository;

import com.trancuong.ecommerce.order.domain.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(UUID orderId);

    @org.springframework.data.jpa.repository.Query("SELECT i.product.id, i.productName, SUM(i.quantity), SUM(i.subtotal) FROM OrderItem i WHERE i.order.status = 'COMPLETED' GROUP BY i.product.id, i.productName ORDER BY SUM(i.quantity) DESC")
    List<Object[]> findTopSellingProducts(org.springframework.data.domain.Pageable pageable);
}
