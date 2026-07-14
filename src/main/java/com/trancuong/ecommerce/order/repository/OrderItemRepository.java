package com.trancuong.ecommerce.order.repository;

import com.trancuong.ecommerce.order.domain.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderIdOrderByCreatedAtAsc(UUID orderId);
}
