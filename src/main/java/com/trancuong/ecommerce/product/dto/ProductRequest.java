package com.trancuong.ecommerce.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 200) String name,
        @NotBlank
        @Size(max = 220)
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "must contain only lowercase letters, numbers, and single hyphens"
        )
        String slug,
        @Size(max = 5000) String description,
        @NotNull
        @DecimalMin(value = "0.00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal price,
        @Size(max = 2000) String imageUrl,
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "must be ACTIVE or INACTIVE")
        String status
) {
}
