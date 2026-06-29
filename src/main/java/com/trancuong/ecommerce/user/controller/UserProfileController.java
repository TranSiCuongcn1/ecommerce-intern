package com.trancuong.ecommerce.user.controller;

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
    public UserProfileResponse getProfile(@AuthenticationPrincipal User user) {
        return userProfileService.getProfile(user);
    }

    @GetMapping("/addresses")
    public List<UserAddressResponse> getAddresses(@AuthenticationPrincipal User user) {
        return userProfileService.getAddresses(user);
    }

    @PostMapping("/addresses")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAddressResponse createAddress(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return userProfileService.createAddress(user, request);
    }

    @PutMapping("/addresses/{id}")
    public UserAddressResponse updateAddress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody UserAddressRequest request
    ) {
        return userProfileService.updateAddress(user, id, request);
    }

    @DeleteMapping("/addresses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAddress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        userProfileService.deleteAddress(user, id);
    }

    @PatchMapping("/addresses/{id}/default")
    public UserAddressResponse setDefaultAddress(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        return userProfileService.setDefaultAddress(user, id);
    }
}
