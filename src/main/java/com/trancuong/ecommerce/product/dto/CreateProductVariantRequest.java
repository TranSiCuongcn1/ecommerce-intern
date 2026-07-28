package com.trancuong.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

public record CreateProductVariantRequest(
        @NotBlank(message = "SKU is required")
        String sku,

        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        BigDecimal price,

        String imageUrl,
        String attributesSummary,
        String status
) {
}
