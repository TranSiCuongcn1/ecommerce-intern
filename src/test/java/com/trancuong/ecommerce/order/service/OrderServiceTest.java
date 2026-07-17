package com.trancuong.ecommerce.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.cart.domain.CartItem;
import com.trancuong.ecommerce.cart.repository.CartItemRepository;
import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.inventory.domain.Inventory;
import com.trancuong.ecommerce.inventory.repository.InventoryRepository;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.OrderItem;
import com.trancuong.ecommerce.order.domain.Payment;
import com.trancuong.ecommerce.order.dto.CheckoutRequest;
import com.trancuong.ecommerce.order.dto.OrderResponse;
import com.trancuong.ecommerce.order.dto.OrderStatusUpdateRequest;
import com.trancuong.ecommerce.order.exception.InvalidOrderStatusException;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.order.repository.PaymentRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.domain.UserAddress;
import com.trancuong.ecommerce.user.repository.UserAddressRepository;
import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private UserAddressRepository userAddressRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void checkout_createsOrderDeductsInventoryAndClearsCart() {
        User user = user();
        Product product = product();
        Warehouse warehouse = warehouse();
        Inventory inventory = inventory(product, warehouse, 100);
        CartItem cartItem = new CartItem(user, product, 2);
        UserAddress address = address(user);

        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(cartItem));
        when(userAddressRepository.findByIdAndUserId(address.getId(), user.getId()))
                .thenReturn(Optional.of(address));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
            return order;
        });
        when(inventoryRepository.findByProductIdForUpdate(product.getId()))
                .thenReturn(List.of(inventory));
        when(orderItemRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.checkout(
                user,
                new CheckoutRequest(address.getId(), "COD", BigDecimal.ZERO)
        );

        assertThat(response.status()).isEqualTo("PENDING");
        assertThat(response.paymentStatus()).isEqualTo("UNPAID");
        assertThat(response.totalAmount()).isEqualByComparingTo("200.00");
        assertThat(response.items()).hasSize(1);
        assertThat(inventory.getQuantityOnHand()).isEqualTo(98);
        verify(cartItemRepository).deleteByUserId(user.getId());
        verify(cartItemRepository).flush();
        verify(orderRepository).flush();
    }

    @Test
    void updateStatus_whenCancellingOrder_restoresInventory() {
        User user = user();
        Product product = product();
        Warehouse warehouse = warehouse();
        Inventory inventory = inventory(product, warehouse, 98);
        Order order = order(user, "PENDING");
        OrderItem orderItem = new OrderItem(
                order,
                product,
                warehouse,
                product.getName(),
                product.getPrice(),
                2,
                new BigDecimal("200.00")
        );

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId()))
                .thenReturn(List.of(orderItem));
        when(inventoryRepository.findByProductIdAndWarehouseIdForUpdate(product.getId(), warehouse.getId()))
                .thenReturn(Optional.of(inventory));

        OrderResponse response = orderService.updateStatus(
                order.getId(),
                new OrderStatusUpdateRequest("CANCELLED")
        );

        assertThat(response.status()).isEqualTo("CANCELLED");
        assertThat(inventory.getQuantityOnHand()).isEqualTo(100);
        verify(orderRepository).flush();
    }

    @Test
    void updateStatus_whenOrderCompleted_throwsException() {
        Order order = order(user(), "COMPLETED");

        when(orderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(
                order.getId(),
                new OrderStatusUpdateRequest("CANCELLED")
        )).isInstanceOf(InvalidOrderStatusException.class);
    }

    private User user() {
        User user = new User(
                "Test User",
                "customer@example.com",
                "password-hash",
                Role.CUSTOMER
        );
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private Product product() {
        Category category = new Category("Phones", "phones");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());

        Product product = new Product(
                category,
                "iPhone 15",
                "iphone-15",
                "Apple smartphone",
                new BigDecimal("100.00"),
                "https://example.com/iphone-15.jpg",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        return product;
    }

    private Warehouse warehouse() {
        Warehouse warehouse = new Warehouse(
                "HCM-01",
                "Ho Chi Minh Warehouse",
                "District 1, Ho Chi Minh",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(warehouse, "id", UUID.randomUUID());
        return warehouse;
    }

    private Inventory inventory(Product product, Warehouse warehouse, int quantityOnHand) {
        Inventory inventory = new Inventory(product, warehouse, quantityOnHand, 0, 10);
        ReflectionTestUtils.setField(inventory, "id", UUID.randomUUID());
        return inventory;
    }

    private UserAddress address(User user) {
        UserAddress address = new UserAddress(
                user,
                "Test Customer",
                "0900000000",
                "Ho Chi Minh",
                "District 1",
                "Ben Nghe",
                "123 Nguyen Hue",
                true
        );
        ReflectionTestUtils.setField(address, "id", UUID.randomUUID());
        return address;
    }

    private Order order(User user, String status) {
        Order order = new Order(
                user,
                new BigDecimal("200.00"),
                status,
                "UNPAID",
                "123 Nguyen Hue, Ben Nghe, District 1, Ho Chi Minh",
                "Test Customer",
                "0900000000",
                BigDecimal.ZERO,
                "COD"
        );
        ReflectionTestUtils.setField(order, "id", UUID.randomUUID());
        return order;
    }
}
