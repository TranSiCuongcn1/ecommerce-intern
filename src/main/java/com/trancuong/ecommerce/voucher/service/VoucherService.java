package com.trancuong.ecommerce.voucher.service;

import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.voucher.domain.Voucher;
import com.trancuong.ecommerce.voucher.domain.VoucherUsage;
import com.trancuong.ecommerce.voucher.dto.ApplyVoucherRequest;
import com.trancuong.ecommerce.voucher.dto.ApplyVoucherResponse;
import com.trancuong.ecommerce.voucher.dto.CreateVoucherRequest;
import com.trancuong.ecommerce.voucher.dto.VoucherResponse;
import com.trancuong.ecommerce.voucher.exception.DuplicateVoucherCodeException;
import com.trancuong.ecommerce.voucher.exception.InvalidVoucherException;
import com.trancuong.ecommerce.voucher.repository.VoucherRepository;
import com.trancuong.ecommerce.voucher.repository.VoucherUsageRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    public record VoucherDiscountResult(Voucher voucher, BigDecimal discountAmount) {}

    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        String code = request.code().trim().toUpperCase();
        if (voucherRepository.existsByCodeIgnoreCase(code)) {
            throw new DuplicateVoucherCodeException(code);
        }

        Voucher voucher = new Voucher(
                code,
                request.discountType(),
                request.discountValue(),
                request.maxDiscountAmount(),
                request.minOrderAmount(),
                request.usageLimit(),
                request.userLimit() != null ? request.userLimit() : 1,
                request.startDate(),
                request.endDate()
        );

        return VoucherResponse.fromEntity(voucherRepository.save(voucher));
    }

    public List<VoucherResponse> getAllVouchers() {
        return voucherRepository.findAll().stream()
                .map(VoucherResponse::fromEntity)
                .toList();
    }

    public List<VoucherResponse> getActiveVouchers() {
        return voucherRepository.findByActiveTrue().stream()
                .filter(v -> {
                    LocalDateTime now = LocalDateTime.now();
                    return (v.getStartDate() == null || !now.isBefore(v.getStartDate())) &&
                            (v.getEndDate() == null || !now.isAfter(v.getEndDate())) &&
                            (v.getUsageLimit() == null || v.getUsedCount() < v.getUsageLimit());
                })
                .map(VoucherResponse::fromEntity)
                .toList();
    }

    public ApplyVoucherResponse applyVoucher(UUID userId, ApplyVoucherRequest request) {
        VoucherDiscountResult result = validateAndCalculateVoucher(userId, request.voucherCode(), request.orderTotal());
        BigDecimal finalTotal = request.orderTotal().subtract(result.discountAmount()).max(BigDecimal.ZERO);
        return new ApplyVoucherResponse(
                result.voucher().getCode(),
                request.orderTotal(),
                result.discountAmount(),
                finalTotal
        );
    }

    public VoucherDiscountResult validateAndCalculateVoucher(UUID userId, String voucherCode, BigDecimal orderTotal) {
        if (voucherCode == null || voucherCode.isBlank()) {
            throw new InvalidVoucherException("Voucher code cannot be empty");
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(voucherCode.trim())
                .orElseThrow(() -> new InvalidVoucherException("Voucher code not found: " + voucherCode));

        long userUsageCount = voucherUsageRepository.countByUserIdAndVoucherId(userId, voucher.getId());
        LocalDateTime now = LocalDateTime.now();

        if (!voucher.isValid(now, orderTotal, userUsageCount)) {
            if (!voucher.isActive()) {
                throw new InvalidVoucherException("Voucher is not active");
            }
            if (voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) {
                throw new InvalidVoucherException("Voucher is not valid yet");
            }
            if (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate())) {
                throw new InvalidVoucherException("Voucher has expired");
            }
            if (voucher.getMinOrderAmount() != null && orderTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
                throw new InvalidVoucherException("Order total is below minimum required: $" + voucher.getMinOrderAmount());
            }
            if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new InvalidVoucherException("Voucher total usage limit has been reached");
            }
            if (userUsageCount >= voucher.getUserLimit()) {
                throw new InvalidVoucherException("You have reached the usage limit for this voucher");
            }
            throw new InvalidVoucherException("Voucher is invalid for this order");
        }

        BigDecimal discount = voucher.calculateDiscount(orderTotal);
        return new VoucherDiscountResult(voucher, discount);
    }

    @Transactional
    public void recordVoucherUsage(Voucher voucher, User user, Order order) {
        voucher.incrementUsedCount();
        voucherRepository.save(voucher);
        VoucherUsage usage = new VoucherUsage(voucher, user, order);
        voucherUsageRepository.save(usage);
    }
}
