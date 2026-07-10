package com.trancuong.ecommerce.cart.repository;

import com.trancuong.ecommerce.cart.domain.CartItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<CartItem> findByIdAndUserId(UUID id, UUID userId);

    Optional<CartItem> findByUserIdAndProductId(UUID userId, UUID productId);

    void deleteByUserId(UUID userId);
}
