package com.trancuong.ecommerce.order.service;

import com.trancuong.ecommerce.cart.domain.CartItem;
import com.trancuong.ecommerce.cart.exception.ProductNotAvailableForCartException;
import com.trancuong.ecommerce.cart.repository.CartItemRepository;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.common.api.PageableDefaults;
import com.trancuong.ecommerce.inventory.domain.Inventory;
import com.trancuong.ecommerce.inventory.exception.InsufficientInventoryException;
import com.trancuong.ecommerce.inventory.repository.InventoryRepository;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.domain.OrderItem;
import com.trancuong.ecommerce.order.domain.Payment;
import com.trancuong.ecommerce.order.dto.CheckoutRequest;
import com.trancuong.ecommerce.order.dto.OrderItemResponse;
import com.trancuong.ecommerce.order.dto.OrderItemResponse.ProductSummary;
import com.trancuong.ecommerce.order.dto.OrderItemResponse.WarehouseSummary;
import com.trancuong.ecommerce.order.dto.OrderResponse;
import com.trancuong.ecommerce.order.exception.CheckoutAddressNotFoundException;
import com.trancuong.ecommerce.order.exception.EmptyCartException;
import com.trancuong.ecommerce.order.exception.OrderNotFoundException;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.order.repository.PaymentRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.domain.UserAddress;
import com.trancuong.ecommerce.user.repository.UserAddressRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private static final String ORDER_STATUS_PENDING = "PENDING";
    private static final String PAYMENT_STATUS_UNPAID = "UNPAID";
    private static final String PAYMENT_PROVIDER_COD = "COD";
    private static final String PAYMENT_STATUS_PENDING = "PENDING";

    private final CartItemRepository cartItemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final UserAddressRepository userAddressRepository;

    public PageResponse<OrderResponse> findMyOrders(User user, Pageable pageable) {
        Pageable sortedPageable = PageableDefaults.withDefaultSort(
                pageable,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.from(orderRepository
                .findByUserId(user.getId(), sortedPageable)
                .map(order -> toResponse(
                        order,
                        orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())
                )));
    }

    public OrderResponse findMyOrderById(User user, UUID id) {
        Order order = orderRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new OrderNotFoundException(id));
        return toResponse(
                order,
                orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())
        );
    }

    @Transactional
    public OrderResponse checkout(User user, CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (cartItems.isEmpty()) {
            throw new EmptyCartException();
        }

        UserAddress address = getCheckoutAddress(user, request.addressId());
        BigDecimal shippingFee = normalizeShippingFee(request.shippingFee());
        String paymentMethod = normalizePaymentMethod(request.paymentMethod());

        BigDecimal itemsTotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalAmount = itemsTotal.add(shippingFee);

        Order order = orderRepository.save(new Order(
                user,
                totalAmount,
                ORDER_STATUS_PENDING,
                PAYMENT_STATUS_UNPAID,
                formatShippingAddress(address),
                address.getReceiverName(),
                address.getReceiverPhone(),
                shippingFee,
                paymentMethod
        ));

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> createOrderItem(order, item))
                .toList();
        orderItemRepository.saveAll(orderItems);
        paymentRepository.save(new Payment(
                order,
                totalAmount,
                paymentMethod,
                PAYMENT_STATUS_PENDING
        ));

        cartItemRepository.deleteByUserId(user.getId());
        cartItemRepository.flush();
        orderRepository.flush();

        return toResponse(order, orderItems);
    }

    private OrderItem createOrderItem(Order order, CartItem cartItem) {
        Product product = cartItem.getProduct();
        if (!"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new ProductNotAvailableForCartException(product.getId());
        }

        Inventory inventory = inventoryRepository.findByProductIdForUpdate(product.getId())
                .stream()
                .filter(item -> "ACTIVE".equalsIgnoreCase(item.getWarehouse().getStatus()))
                .filter(item -> item.getAvailableQuantity() >= cartItem.getQuantity())
                .min(Comparator
                        .comparingInt(Inventory::getAvailableQuantity)
                        .thenComparing(item -> item.getWarehouse().getCode()))
                .orElseThrow(() -> new InsufficientInventoryException(
                        product.getId(),
                        cartItem.getQuantity()
                ));

        inventory.deduct(cartItem.getQuantity());
        BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));

        return new OrderItem(
                order,
                product,
                inventory.getWarehouse(),
                product.getName(),
                product.getPrice(),
                cartItem.getQuantity(),
                subtotal
        );
    }

    private UserAddress getCheckoutAddress(User user, UUID addressId) {
        if (addressId != null) {
            return userAddressRepository.findByIdAndUserId(addressId, user.getId())
                    .orElseThrow(CheckoutAddressNotFoundException::new);
        }
        return userAddressRepository.findByUserIdAndDefaultAddressTrue(user.getId())
                .orElseThrow(CheckoutAddressNotFoundException::new);
    }

    private BigDecimal normalizeShippingFee(BigDecimal shippingFee) {
        if (shippingFee == null) {
            return BigDecimal.ZERO;
        }
        return shippingFee;
    }

    private String normalizePaymentMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            return PAYMENT_PROVIDER_COD;
        }
        return paymentMethod.trim().toUpperCase();
    }

    private String formatShippingAddress(UserAddress address) {
        return address.getDetailAddress()
                + ", " + address.getWard()
                + ", " + address.getDistrict()
                + ", " + address.getProvince();
    }

    private OrderResponse toResponse(Order order, List<OrderItem> items) {
        List<OrderItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getPaymentStatus(),
                order.getPaymentMethod(),
                order.getShippingFee(),
                order.getTotalAmount(),
                order.getReceiverName(),
                order.getReceiverPhone(),
                order.getShippingAddress(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        Product product = item.getProduct();
        return new OrderItemResponse(
                item.getId(),
                product == null ? null : new ProductSummary(product.getId(), product.getName(), product.getSlug()),
                item.getWarehouse() == null ? null : new WarehouseSummary(
                        item.getWarehouse().getId(),
                        item.getWarehouse().getCode(),
                        item.getWarehouse().getName()
                ),
                item.getProductName(),
                item.getUnitPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
