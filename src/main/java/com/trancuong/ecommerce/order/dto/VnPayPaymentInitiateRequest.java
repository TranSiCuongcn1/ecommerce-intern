package com.trancuong.ecommerce.order.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VnPayPaymentInitiateRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,
        String returnUrl
) {
}
