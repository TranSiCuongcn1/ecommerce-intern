package com.trancuong.ecommerce.wishlist.service;

import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.common.api.PageableDefaults;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.wishlist.domain.WishlistItem;
import com.trancuong.ecommerce.wishlist.dto.WishlistItemResponse;
import com.trancuong.ecommerce.wishlist.exception.DuplicateWishlistItemException;
import com.trancuong.ecommerce.wishlist.exception.WishlistItemNotFoundException;
import com.trancuong.ecommerce.wishlist.repository.WishlistRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    @Transactional
    public WishlistItemResponse addToWishlist(User user, UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new DuplicateWishlistItemException();
        }

        WishlistItem item = new WishlistItem(user, product);
        return WishlistItemResponse.fromEntity(wishlistRepository.save(item));
    }

    @Transactional
    public void removeFromWishlist(User user, UUID productId) {
        if (!wishlistRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new WishlistItemNotFoundException();
        }
        wishlistRepository.deleteByUserIdAndProductId(user.getId(), productId);
    }

    public PageResponse<WishlistItemResponse> getMyWishlist(User user, Pageable pageable) {
        Pageable sortedPageable = PageableDefaults.withDefaultSort(
                pageable,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.from(wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), sortedPageable)
                .map(WishlistItemResponse::fromEntity));
    }
}
