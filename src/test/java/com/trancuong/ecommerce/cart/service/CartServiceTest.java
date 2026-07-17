package com.trancuong.ecommerce.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.cart.domain.CartItem;
import com.trancuong.ecommerce.cart.dto.CartItemRequest;
import com.trancuong.ecommerce.cart.dto.CartResponse;
import com.trancuong.ecommerce.cart.exception.ProductNotAvailableForCartException;
import com.trancuong.ecommerce.cart.repository.CartItemRepository;
import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
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
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void addItem_whenItemAlreadyExists_increasesQuantityAndReturnsCartTotal() {
        User user = user();
        Product product = product("ACTIVE", new BigDecimal("100.00"));
        CartItem existingItem = new CartItem(user, product, 1);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findByUserIdAndProductId(user.getId(), product.getId()))
                .thenReturn(Optional.of(existingItem));
        when(cartItemRepository.findByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(List.of(existingItem));

        CartResponse response = cartService.addItem(
                user,
                new CartItemRequest(product.getId(), 2)
        );

        assertThat(existingItem.getQuantity()).isEqualTo(3);
        assertThat(response.totalItems()).isEqualTo(3);
        assertThat(response.totalAmount()).isEqualByComparingTo("300.00");
        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartItemRepository).flush();
    }

    @Test
    void addItem_whenProductInactive_throwsException() {
        User user = user();
        Product product = product("INACTIVE", new BigDecimal("100.00"));

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> cartService.addItem(
                user,
                new CartItemRequest(product.getId(), 1)
        )).isInstanceOf(ProductNotAvailableForCartException.class);
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

    private Product product(String status, BigDecimal price) {
        Category category = new Category("Phones", "phones");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());

        Product product = new Product(
                category,
                "iPhone 15",
                "iphone-15",
                "Apple smartphone",
                price,
                "https://example.com/iphone-15.jpg",
                status
        );
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        return product;
    }
}
