package com.trancuong.ecommerce.auth.controller;

import com.trancuong.ecommerce.auth.dto.AuthResponse;
import com.trancuong.ecommerce.auth.dto.LoginRequest;
import com.trancuong.ecommerce.auth.dto.LogoutRequest;
import com.trancuong.ecommerce.auth.dto.RefreshTokenRequest;
import com.trancuong.ecommerce.auth.dto.RegisterRequest;
import com.trancuong.ecommerce.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(authorizationHeader, request);
    }
}
