package com.trancuong.ecommerce.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        UserAddressResponse defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
