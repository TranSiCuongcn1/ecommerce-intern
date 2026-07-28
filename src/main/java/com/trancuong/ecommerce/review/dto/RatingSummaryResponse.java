package com.trancuong.ecommerce.review.dto;

import java.util.UUID;

public record RatingSummaryResponse(
        UUID productId,
        Double averageRating,
        long totalReviews
) {
}
