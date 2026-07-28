package com.trancuong.ecommerce.product.service;

import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.domain.ProductVariant;
import com.trancuong.ecommerce.product.dto.CreateProductVariantRequest;
import com.trancuong.ecommerce.product.dto.ProductVariantResponse;
import com.trancuong.ecommerce.product.exception.DuplicateSkuException;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.product.exception.ProductVariantNotFoundException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.product.repository.ProductVariantRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;

    @Transactional
    public ProductVariantResponse createVariant(UUID productId, CreateProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        String sku = request.sku().trim().toUpperCase();
        if (variantRepository.existsBySkuIgnoreCase(sku)) {
            throw new DuplicateSkuException(sku);
        }

        ProductVariant variant = new ProductVariant(
                product,
                sku,
                request.price(),
                request.imageUrl(),
                request.attributesSummary(),
                request.status()
        );

        return ProductVariantResponse.fromEntity(variantRepository.save(variant));
    }

    public List<ProductVariantResponse> getVariantsByProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        return variantRepository.findByProductId(productId).stream()
                .map(ProductVariantResponse::fromEntity)
                .toList();
    }

    public ProductVariantResponse getVariantById(UUID variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException(variantId));
        return ProductVariantResponse.fromEntity(variant);
    }

    @Transactional
    public void deleteVariant(UUID variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new ProductVariantNotFoundException(variantId));
        variantRepository.delete(variant);
    }
}
