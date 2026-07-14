package com.trancuong.ecommerce.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.UUID;

public record CheckoutRequest(
        UUID addressId,
        @Pattern(regexp = "(?i)COD|BANK_TRANSFER", message = "must be COD or BANK_TRANSFER")
        String paymentMethod,
        @DecimalMin(value = "0.00")
        @Digits(integer = 13, fraction = 2)
        BigDecimal shippingFee
) {
}
