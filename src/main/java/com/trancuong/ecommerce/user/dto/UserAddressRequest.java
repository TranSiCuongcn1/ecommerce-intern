package com.trancuong.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAddressRequest(
        @NotBlank @Size(max = 150) String receiverName,
        @NotBlank @Size(max = 20) String receiverPhone,
        @NotBlank @Size(max = 100) String province,
        @NotBlank @Size(max = 100) String district,
        @NotBlank @Size(max = 100) String ward,
        @NotBlank String detailAddress,
        boolean defaultAddress
) {
}
