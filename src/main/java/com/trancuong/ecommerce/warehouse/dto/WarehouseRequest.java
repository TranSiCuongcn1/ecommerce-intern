package com.trancuong.ecommerce.warehouse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WarehouseRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String address,
        @NotBlank
        @Size(max = 30)
        @Pattern(regexp = "(?i)ACTIVE|INACTIVE", message = "must be ACTIVE or INACTIVE")
        String status
) {
}
