package com.trancuong.ecommerce.voucher.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.voucher.domain.DiscountType;
import com.trancuong.ecommerce.voucher.domain.Voucher;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private VoucherUsageRepository voucherUsageRepository;

    @InjectMocks
    private VoucherService voucherService;

    @Test
    void createVoucher_createsAndReturnsVoucher() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "SALE20",
                DiscountType.PERCENTAGE,
                new BigDecimal("20.00"),
                new BigDecimal("50.00"),
                new BigDecimal("100.00"),
                100,
                1,
                null,
                null
        );

        when(voucherRepository.existsByCodeIgnoreCase("SALE20")).thenReturn(false);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> {
            Voucher v = invocation.getArgument(0);
            ReflectionTestUtils.setField(v, "id", UUID.randomUUID());
            return v;
        });

        VoucherResponse response = voucherService.createVoucher(request);

        assertThat(response.code()).isEqualTo("SALE20");
        assertThat(response.discountType()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(response.discountValue()).isEqualByComparingTo("20.00");
    }

    @Test
    void createVoucher_whenDuplicateCode_throwsException() {
        CreateVoucherRequest request = new CreateVoucherRequest(
                "SALE20",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("50.00"),
                null, null, null, null, null, null
        );

        when(voucherRepository.existsByCodeIgnoreCase("SALE20")).thenReturn(true);

        assertThatThrownBy(() -> voucherService.createVoucher(request))
                .isInstanceOf(DuplicateVoucherCodeException.class);
    }

    @Test
    void applyVoucher_calculatesDiscountCorrectly() {
        UUID userId = UUID.randomUUID();
        Voucher voucher = new Voucher(
                "SALE10",
                DiscountType.PERCENTAGE,
                new BigDecimal("10.00"),
                null,
                new BigDecimal("100.00"),
                null,
                1,
                null,
                null
        );
        ReflectionTestUtils.setField(voucher, "id", UUID.randomUUID());

        when(voucherRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(voucher));
        when(voucherUsageRepository.countByUserIdAndVoucherId(userId, voucher.getId())).thenReturn(0L);

        ApplyVoucherResponse response = voucherService.applyVoucher(
                userId,
                new ApplyVoucherRequest("SALE10", new BigDecimal("200.00"))
        );

        assertThat(response.discountAmount()).isEqualByComparingTo("20.00");
        assertThat(response.finalTotal()).isEqualByComparingTo("180.00");
    }

    @Test
    void applyVoucher_whenOrderBelowMinAmount_throwsException() {
        UUID userId = UUID.randomUUID();
        Voucher voucher = new Voucher(
                "SALE10",
                DiscountType.PERCENTAGE,
                new BigDecimal("10.00"),
                null,
                new BigDecimal("500.00"),
                null,
                1,
                null,
                null
        );
        ReflectionTestUtils.setField(voucher, "id", UUID.randomUUID());

        when(voucherRepository.findByCodeIgnoreCase("SALE10")).thenReturn(Optional.of(voucher));

        assertThatThrownBy(() -> voucherService.applyVoucher(
                userId,
                new ApplyVoucherRequest("SALE10", new BigDecimal("200.00"))
        )).isInstanceOf(InvalidVoucherException.class)
                .hasMessageContaining("minimum required");
    }
}
