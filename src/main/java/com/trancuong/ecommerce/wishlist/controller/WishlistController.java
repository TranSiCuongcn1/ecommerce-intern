package com.trancuong.ecommerce.wishlist.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.wishlist.dto.WishlistItemResponse;
import com.trancuong.ecommerce.wishlist.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Wishlist", description = "Customer Wishlist Management APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/me/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    @Operation(summary = "Add product to wishlist")
    @PostMapping("/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WishlistItemResponse> addToWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable UUID productId
    ) {
        return ApiResponse.success(201, "Product added to wishlist", wishlistService.addToWishlist(user, productId));
    }

    @Operation(summary = "Remove product from wishlist")
    @DeleteMapping("/{productId}")
    public ApiResponse<String> removeFromWishlist(
            @AuthenticationPrincipal User user,
            @PathVariable UUID productId
    ) {
        wishlistService.removeFromWishlist(user, productId);
        return ApiResponse.success(200, "Product removed from wishlist", "Product removed from wishlist");
    }

    @Operation(summary = "Get my wishlist")
    @GetMapping
    public ApiResponse<PageResponse<WishlistItemResponse>> getMyWishlist(
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return ApiResponse.success(wishlistService.getMyWishlist(user, pageable));
    }
}
