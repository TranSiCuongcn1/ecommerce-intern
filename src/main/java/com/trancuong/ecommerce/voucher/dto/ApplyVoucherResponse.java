package com.trancuong.ecommerce.voucher.dto;

import java.math.BigDecimal;

public record ApplyVoucherResponse(
        String voucherCode,
        BigDecimal originalTotal,
        BigDecimal discountAmount,
        BigDecimal finalTotal
) {
}
