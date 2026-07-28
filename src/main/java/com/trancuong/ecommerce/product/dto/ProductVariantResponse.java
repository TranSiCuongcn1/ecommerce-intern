package com.trancuong.ecommerce.product.dto;

import com.trancuong.ecommerce.product.domain.ProductVariant;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        UUID productId,
        String sku,
        BigDecimal price,
        BigDecimal effectivePrice,
        String imageUrl,
        String attributesSummary,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductVariantResponse fromEntity(ProductVariant variant) {
        return new ProductVariantResponse(
                variant.getId(),
                variant.getProduct().getId(),
                variant.getSku(),
                variant.getPrice(),
                variant.getEffectivePrice(),
                variant.getImageUrl(),
                variant.getAttributesSummary(),
                variant.getStatus(),
                variant.getCreatedAt(),
                variant.getUpdatedAt()
        );
    }
}
