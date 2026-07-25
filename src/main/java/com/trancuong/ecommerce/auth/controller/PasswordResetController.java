package com.trancuong.ecommerce.auth.controller;

import com.trancuong.ecommerce.auth.dto.ForgotPasswordRequest;
import com.trancuong.ecommerce.auth.dto.ResetPasswordRequest;
import com.trancuong.ecommerce.auth.service.PasswordResetService;
import com.trancuong.ecommerce.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Authentication and Password Management APIs")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(summary = "Request password reset email")
    @PostMapping("/forgot-password")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.forgotPassword(request);
        String msg = "If an account with that email exists, a password reset email has been sent.";
        return ApiResponse.success(200, msg, msg);
    }

    @Operation(summary = "Reset password with token")
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request);
        String msg = "Password has been reset successfully.";
        return ApiResponse.success(200, msg, msg);
    }
}
