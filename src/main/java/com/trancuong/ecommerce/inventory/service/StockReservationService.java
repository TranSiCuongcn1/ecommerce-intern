package com.trancuong.ecommerce.inventory.service;

import com.trancuong.ecommerce.inventory.domain.Inventory;
import com.trancuong.ecommerce.inventory.exception.InsufficientInventoryException;
import com.trancuong.ecommerce.inventory.repository.InventoryRepository;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.OrderItem;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockReservationService {

    private static final String RESERVATION_KEY_PREFIX = "stock_reservation:order:";
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(15);

    private final InventoryRepository inventoryRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void reserveStock(UUID orderId, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getProduct() == null || item.getWarehouse() == null) {
                continue;
            }
            Inventory inventory = inventoryRepository.findByProductIdAndWarehouseIdForUpdate(
                            item.getProduct().getId(),
                            item.getWarehouse().getId()
                    )
                    .orElseThrow(() -> new InsufficientInventoryException(
                            item.getProduct().getId(),
                            item.getQuantity()
                    ));

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new InsufficientInventoryException(item.getProduct().getId(), item.getQuantity());
            }

            inventory.reserve(item.getQuantity());
            inventoryRepository.save(inventory);
        }

        String key = RESERVATION_KEY_PREFIX + orderId;
        redisTemplate.opsForValue().set(key, "RESERVED", RESERVATION_TTL);
        log.info("Stock reserved for order {} for 15 minutes", orderId);
    }

    @Transactional
    public void releaseReservation(Order order, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getProduct() == null || item.getWarehouse() == null) {
                continue;
            }
            inventoryRepository.findByProductIdAndWarehouseIdForUpdate(
                    item.getProduct().getId(),
                    item.getWarehouse().getId()
            ).ifPresent(inventory -> {
                inventory.releaseReservation(item.getQuantity());
                inventoryRepository.save(inventory);
            });
        }

        String key = RESERVATION_KEY_PREFIX + order.getId();
        redisTemplate.delete(key);
        log.info("Stock reservation released for order {}", order.getId());
    }

    @Transactional
    public void confirmReservation(Order order, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            if (item.getProduct() == null || item.getWarehouse() == null) {
                continue;
            }
            inventoryRepository.findByProductIdAndWarehouseIdForUpdate(
                    item.getProduct().getId(),
                    item.getWarehouse().getId()
            ).ifPresent(inventory -> {
                inventory.confirmReservation(item.getQuantity());
                inventoryRepository.save(inventory);
            });
        }

        String key = RESERVATION_KEY_PREFIX + order.getId();
        redisTemplate.delete(key);
        log.info("Stock reservation confirmed and deducted for order {}", order.getId());
    }
}
