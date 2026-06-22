package com.trancuong.ecommerce.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank
        @Size(max = 180)
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "must contain only lowercase letters, numbers, and single hyphens"
        )
        String slug
) {
}
