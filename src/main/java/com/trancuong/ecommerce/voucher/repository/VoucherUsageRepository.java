package com.trancuong.ecommerce.voucher.repository;

import com.trancuong.ecommerce.voucher.domain.Voucher;
import com.trancuong.ecommerce.voucher.domain.VoucherUsage;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUsageRepository extends JpaRepository<VoucherUsage, UUID> {
    long countByUserIdAndVoucherId(UUID userId, UUID voucherId);
}
