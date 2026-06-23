package com.trancuong.ecommerce.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserSummary user
) {

    public record UserSummary(
            UUID id,
            String fullName,
            String email,
            String role
    ) {
    }
}
