package com.trancuong.ecommerce.wishlist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.wishlist.domain.WishlistItem;
import com.trancuong.ecommerce.wishlist.dto.WishlistItemResponse;
import com.trancuong.ecommerce.wishlist.exception.DuplicateWishlistItemException;
import com.trancuong.ecommerce.wishlist.exception.WishlistItemNotFoundException;
import com.trancuong.ecommerce.wishlist.repository.WishlistRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private WishlistService wishlistService;

    @Test
    void addToWishlist_addsProductToWishlist() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        UUID productId = UUID.randomUUID();
        Product product = product(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);
        when(wishlistRepository.save(any(WishlistItem.class))).thenAnswer(inv -> {
            WishlistItem item = inv.getArgument(0);
            ReflectionTestUtils.setField(item, "id", UUID.randomUUID());
            return item;
        });

        WishlistItemResponse response = wishlistService.addToWishlist(user, productId);

        assertThat(response.productId()).isEqualTo(productId);
        assertThat(response.productName()).isEqualTo("iPhone 15");
    }

    @Test
    void addToWishlist_whenDuplicate_throwsException() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        UUID productId = UUID.randomUUID();
        Product product = product(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

        assertThatThrownBy(() -> wishlistService.addToWishlist(user, productId))
                .isInstanceOf(DuplicateWishlistItemException.class);
    }

    @Test
    void removeFromWishlist_removesProduct() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);
        UUID productId = UUID.randomUUID();

        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

        wishlistService.removeFromWishlist(user, productId);

        verify(wishlistRepository).deleteByUserIdAndProductId(userId, productId);
    }

    @Test
    void removeFromWishlist_whenNotFound_throwsException() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);
        UUID productId = UUID.randomUUID();

        when(wishlistRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

        assertThatThrownBy(() -> wishlistService.removeFromWishlist(user, productId))
                .isInstanceOf(WishlistItemNotFoundException.class);
    }

    private Product product(UUID id) {
        Category category = new Category("Phones", "phones");
        Product product = new Product(category, "iPhone 15", "iphone-15", "desc", new BigDecimal("20000000"), "img", "ACTIVE");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
