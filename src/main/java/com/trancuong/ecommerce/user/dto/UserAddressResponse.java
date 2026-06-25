package com.trancuong.ecommerce.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserAddressResponse(
        UUID id,
        String receiverName,
        String receiverPhone,
        String province,
        String district,
        String ward,
        String detailAddress,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
