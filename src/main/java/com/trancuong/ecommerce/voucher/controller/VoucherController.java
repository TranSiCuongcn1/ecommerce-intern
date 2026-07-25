package com.trancuong.ecommerce.voucher.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.voucher.dto.ApplyVoucherRequest;
import com.trancuong.ecommerce.voucher.dto.ApplyVoucherResponse;
import com.trancuong.ecommerce.voucher.dto.VoucherResponse;
import com.trancuong.ecommerce.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vouchers", description = "Customer Voucher APIs")
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Get active public vouchers")
    @GetMapping("/active")
    public ApiResponse<List<VoucherResponse>> getActiveVouchers() {
        return ApiResponse.success(voucherService.getActiveVouchers());
    }

    @Operation(summary = "Validate and apply voucher to order total")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/apply")
    public ApiResponse<ApplyVoucherResponse> applyVoucher(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ApplyVoucherRequest request
    ) {
        return ApiResponse.success(voucherService.applyVoucher(user.getId(), request));
    }
}
