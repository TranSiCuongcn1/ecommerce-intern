package com.trancuong.ecommerce.voucher.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ApplyVoucherRequest(
        @NotBlank(message = "Voucher code is required")
        String voucherCode,

        @NotNull(message = "Order total is required")
        @DecimalMin(value = "0.00", message = "Order total cannot be negative")
        BigDecimal orderTotal
) {
}
