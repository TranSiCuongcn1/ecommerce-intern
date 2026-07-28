package com.trancuong.ecommerce.review.dto;

import com.trancuong.ecommerce.review.domain.Review;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID productId,
        UUID userId,
        String userFullName,
        Integer rating,
        String comment,
        Boolean isVerifiedPurchase,
        LocalDateTime createdAt
) {
    public static ReviewResponse fromEntity(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getProduct().getId(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getRating(),
                review.getComment(),
                review.getIsVerifiedPurchase(),
                review.getCreatedAt()
        );
    }
}
