package com.trancuong.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.category.repository.CategoryRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.dto.ProductRequest;
import com.trancuong.ecommerce.product.dto.ProductResponse;
import com.trancuong.ecommerce.product.exception.DuplicateProductSlugException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void create_normalizesTextAndStatus() {
        Category category = category();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.existsBySlug("iphone-15")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
            return product;
        });

        ProductResponse response = productService.create(new ProductRequest(
                category.getId(),
                " iPhone 15 ",
                " iphone-15 ",
                " ",
                new BigDecimal("19990000.00"),
                " ",
                " active "
        ));

        assertThat(response.name()).isEqualTo("iPhone 15");
        assertThat(response.slug()).isEqualTo("iphone-15");
        assertThat(response.description()).isNull();
        assertThat(response.imageUrl()).isNull();
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    void create_whenSlugExists_throwsDuplicateSlug() {
        Category category = category();

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(productRepository.existsBySlug("iphone-15")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(new ProductRequest(
                category.getId(),
                "iPhone 15",
                "iphone-15",
                null,
                new BigDecimal("19990000.00"),
                null,
                "ACTIVE"
        ))).isInstanceOf(DuplicateProductSlugException.class);
    }

    @Test
    void update_updatesProductAndFlushes() {
        Category oldCategory = category();
        Category newCategory = category();
        Product product = product(oldCategory, "ACTIVE");

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(newCategory.getId())).thenReturn(Optional.of(newCategory));
        when(productRepository.existsBySlugAndIdNot("iphone-15-pro", product.getId())).thenReturn(false);

        ProductResponse response = productService.update(product.getId(), new ProductRequest(
                newCategory.getId(),
                " iPhone 15 Pro ",
                " iphone-15-pro ",
                " Pro model ",
                new BigDecimal("29990000.00"),
                " https://example.com/iphone-15-pro.jpg ",
                " inactive "
        ));

        assertThat(response.name()).isEqualTo("iPhone 15 Pro");
        assertThat(response.slug()).isEqualTo("iphone-15-pro");
        assertThat(response.status()).isEqualTo("INACTIVE");
        assertThat(response.category().id()).isEqualTo(newCategory.getId());
        verify(productRepository).flush();
    }

    private Category category() {
        Category category = new Category("Phones", "phones");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
        return category;
    }

    private Product product(Category category, String status) {
        Product product = new Product(
                category,
                "iPhone 15",
                "iphone-15",
                "Apple smartphone",
                new BigDecimal("19990000.00"),
                "https://example.com/iphone-15.jpg",
                status
        );
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        return product;
    }
}
