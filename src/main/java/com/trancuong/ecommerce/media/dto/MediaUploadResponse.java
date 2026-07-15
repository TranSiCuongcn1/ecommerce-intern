package com.trancuong.ecommerce.media.dto;

public record MediaUploadResponse(
        String objectName,
        String url,
        String contentType,
        long size
) {
}
