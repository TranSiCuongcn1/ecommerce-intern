package com.trancuong.ecommerce.order.dto;

public record MomoInitiateResponse(
        String payUrl,
        String qrCodeUrl,
        String deeplink
) {
}
