package com.trancuong.ecommerce.order.repository;

import com.trancuong.ecommerce.order.domain.Order;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
