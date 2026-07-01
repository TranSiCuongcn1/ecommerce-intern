package com.trancuong.ecommerce.user.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.dto.UserAddressRequest;
import com.trancuong.ecommerce.user.dto.UserAddressResponse;
import com.trancuong.ecommerce.user.dto.UserProfileResponse;
import com.trancuong.ecommerce.user.service.UserProfileService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ApiResponse<UserProfileResponse> getProfile(@AuthenticationPrincipal User user) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get profile successfully",
                userProfileService.getProfile(user)
        );
    }

    @GetMapping("/addresses")
    public ApiResponse<List<UserAddressResponse>> getAddresses(@AuthenticationPrincipal User user) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get addresses successfully",
                userProfileService.getAddresses(user)
        );
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserAddressResponse> createAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Create address successfully",
                userProfileService.createAddress(user, request)
        );
    }

    @PutMapping("/addresses/{id}")
    public ApiResponse<UserAddressResponse> updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Update address successfully",
                userProfileService.updateAddress(user, id, request)
        );
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        userProfileService.deleteAddress(user, id);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Delete address successfully",
                null
        );
    }

    @PatchMapping("/addresses/{id}/default")
    public ApiResponse<UserAddressResponse> setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Set default address successfully",
                userProfileService.setDefaultAddress(user, id)
        );
    }
}
