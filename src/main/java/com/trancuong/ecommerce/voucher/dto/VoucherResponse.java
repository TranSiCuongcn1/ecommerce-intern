package com.trancuong.ecommerce.voucher.dto;

import com.trancuong.ecommerce.voucher.domain.DiscountType;
import com.trancuong.ecommerce.voucher.domain.Voucher;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record VoucherResponse(
        UUID id,
        String code,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        Integer usageLimit,
        int usedCount,
        int userLimit,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean active
) {
    public static VoucherResponse fromEntity(Voucher voucher) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMaxDiscountAmount(),
                voucher.getMinOrderAmount(),
                voucher.getUsageLimit(),
                voucher.getUsedCount(),
                voucher.getUserLimit(),
                voucher.getStartDate(),
                voucher.getEndDate(),
                voucher.isActive()
        );
    }
}
