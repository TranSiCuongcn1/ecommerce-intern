package com.trancuong.ecommerce.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OrderStatusUpdateRequest(
        @NotBlank
        @Pattern(
                regexp = "(?i)PENDING|CONFIRMED|SHIPPING|COMPLETED|CANCELLED",
                message = "must be PENDING, CONFIRMED, SHIPPING, COMPLETED, or CANCELLED"
        )
        String status
) {
}
