package com.trancuong.ecommerce.review.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.review.dto.CreateReviewRequest;
import com.trancuong.ecommerce.review.dto.RatingSummaryResponse;
import com.trancuong.ecommerce.review.dto.ReviewResponse;
import com.trancuong.ecommerce.review.service.ReviewService;
import com.trancuong.ecommerce.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Reviews", description = "Product Rating & Review APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Submit a product review (Customer)")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{productId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal User user,
            @PathVariable UUID productId,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ApiResponse.success(201, "Review submitted successfully", reviewService.createReview(user, productId, request));
    }

    @Operation(summary = "Get reviews for a product")
    @GetMapping("/{productId}/reviews")
    public ApiResponse<PageResponse<ReviewResponse>> getReviews(
            @PathVariable UUID productId,
            Pageable pageable
    ) {
        return ApiResponse.success(reviewService.getReviewsByProduct(productId, pageable));
    }

    @Operation(summary = "Get rating summary of a product")
    @GetMapping("/{productId}/rating-summary")
    public ApiResponse<RatingSummaryResponse> getRatingSummary(@PathVariable UUID productId) {
        return ApiResponse.success(reviewService.getRatingSummary(productId));
    }
}
