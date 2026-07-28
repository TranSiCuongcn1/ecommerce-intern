package com.trancuong.ecommerce.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.inventory.domain.Inventory;
import com.trancuong.ecommerce.inventory.repository.InventoryRepository;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.OrderItem;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StockReservationServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private StockReservationService stockReservationService;

    @Test
    void reserveStock_reservesQuantityAndSetsRedisKey() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        Category cat = new Category("Phones", "phones");
        Product prod = new Product(cat, "iPhone", "iphone", "desc", new BigDecimal("100.00"), "img", "ACTIVE");
        Warehouse wh = new Warehouse("W01", "Kho 1", "Address", "ACTIVE");
        ReflectionTestUtils.setField(prod, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(wh, "id", UUID.randomUUID());

        Inventory inventory = new Inventory(prod, wh, 10, 0, 2);
        Order order = new Order(user, new BigDecimal("100.00"), "PENDING", "UNPAID", "Addr", "John", "0900", BigDecimal.ZERO, "MOMO");
        UUID orderId = UUID.randomUUID();
        ReflectionTestUtils.setField(order, "id", orderId);

        OrderItem item = new OrderItem(order, prod, wh, prod.getName(), prod.getPrice(), 2, new BigDecimal("200.00"));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(inventoryRepository.findByProductIdAndWarehouseIdForUpdate(prod.getId(), wh.getId()))
                .thenReturn(Optional.of(inventory));

        stockReservationService.reserveStock(orderId, List.of(item));

        assertThat(inventory.getQuantityReserved()).isEqualTo(2);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(8);
        verify(valueOperations).set(eq("stock_reservation:order:" + orderId), eq("RESERVED"), any(Duration.class));
    }
}
