package com.trancuong.ecommerce.voucher.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.voucher.dto.CreateVoucherRequest;
import com.trancuong.ecommerce.voucher.dto.VoucherResponse;
import com.trancuong.ecommerce.voucher.service.VoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Vouchers", description = "Admin Voucher Management APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final VoucherService voucherService;

    @Operation(summary = "Create a new voucher")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        return ApiResponse.success(voucherService.createVoucher(request));
    }

    @Operation(summary = "Get all vouchers (admin)")
    @GetMapping
    public ApiResponse<List<VoucherResponse>> getAllVouchers() {
        return ApiResponse.success(voucherService.getAllVouchers());
    }
}
