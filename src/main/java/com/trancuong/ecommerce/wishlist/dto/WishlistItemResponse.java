package com.trancuong.ecommerce.wishlist.dto;

import com.trancuong.ecommerce.wishlist.domain.WishlistItem;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record WishlistItemResponse(
        UUID id,
        UUID productId,
        String productName,
        String productSlug,
        BigDecimal productPrice,
        String productImageUrl,
        LocalDateTime createdAt
) {
    public static WishlistItemResponse fromEntity(WishlistItem item) {
        return new WishlistItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getSlug(),
                item.getProduct().getPrice(),
                item.getProduct().getImageUrl(),
                item.getCreatedAt()
        );
    }
}
