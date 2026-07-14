package com.trancuong.ecommerce.order.repository;

import com.trancuong.ecommerce.order.domain.Payment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
}
