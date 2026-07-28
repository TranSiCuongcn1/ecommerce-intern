package com.trancuong.ecommerce.product.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.domain.ProductVariant;
import com.trancuong.ecommerce.product.dto.CreateProductVariantRequest;
import com.trancuong.ecommerce.product.dto.ProductVariantResponse;
import com.trancuong.ecommerce.product.exception.DuplicateSkuException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.product.repository.ProductVariantRepository;
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
class ProductVariantServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private ProductVariantService variantService;

    @Test
    void createVariant_createsAndReturnsVariant() {
        UUID productId = UUID.randomUUID();
        Product product = product(productId);
        CreateProductVariantRequest request = new CreateProductVariantRequest(
                "IP15-RED-128",
                new BigDecimal("25000000"),
                "https://example.com/red.jpg",
                "Color: Red, Storage: 128GB",
                "ACTIVE"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySkuIgnoreCase("IP15-RED-128")).thenReturn(false);
        when(variantRepository.save(any(ProductVariant.class))).thenAnswer(inv -> {
            ProductVariant v = inv.getArgument(0);
            ReflectionTestUtils.setField(v, "id", UUID.randomUUID());
            return v;
        });

        ProductVariantResponse response = variantService.createVariant(productId, request);

        assertThat(response.sku()).isEqualTo("IP15-RED-128");
        assertThat(response.effectivePrice()).isEqualByComparingTo("25000000");
    }

    @Test
    void createVariant_whenDuplicateSku_throwsException() {
        UUID productId = UUID.randomUUID();
        Product product = product(productId);
        CreateProductVariantRequest request = new CreateProductVariantRequest(
                "IP15-RED-128",
                new BigDecimal("25000000"),
                null, null, null
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(variantRepository.existsBySkuIgnoreCase("IP15-RED-128")).thenReturn(true);

        assertThatThrownBy(() -> variantService.createVariant(productId, request))
                .isInstanceOf(DuplicateSkuException.class);
    }

    private Product product(UUID id) {
        Category category = new Category("Phones", "phones");
        Product product = new Product(category, "iPhone 15", "iphone-15", "desc", new BigDecimal("20000000"), "img", "ACTIVE");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
