package com.trancuong.ecommerce.auth.controller;

import com.trancuong.ecommerce.auth.dto.AuthResponse;
import com.trancuong.ecommerce.auth.dto.LoginRequest;
import com.trancuong.ecommerce.auth.dto.LogoutRequest;
import com.trancuong.ecommerce.auth.dto.RefreshTokenRequest;
import com.trancuong.ecommerce.auth.dto.RegisterRequest;
import com.trancuong.ecommerce.auth.service.AuthService;
import com.trancuong.ecommerce.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Register successfully",
                authService.register(request)
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Login successfully",
                authService.login(request)
        );
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Refresh token successfully",
                authService.refresh(request)
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Logout successfully",
                null
        );
    }
}
