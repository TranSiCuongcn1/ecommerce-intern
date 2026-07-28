package com.trancuong.ecommerce.review.service;

import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.common.api.PageableDefaults;
import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.review.domain.Review;
import com.trancuong.ecommerce.review.dto.CreateReviewRequest;
import com.trancuong.ecommerce.review.dto.RatingSummaryResponse;
import com.trancuong.ecommerce.review.dto.ReviewResponse;
import com.trancuong.ecommerce.review.exception.DuplicateReviewException;
import com.trancuong.ecommerce.review.repository.ReviewRepository;
import com.trancuong.ecommerce.user.domain.User;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    @Transactional
    public ReviewResponse createReview(User user, UUID productId, CreateReviewRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (reviewRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new DuplicateReviewException();
        }

        Order order = null;
        boolean isVerified = false;

        if (request.orderId() != null) {
            order = orderRepository.findById(request.orderId()).orElse(null);
            if (order != null && order.getUser().getId().equals(user.getId())
                    && "COMPLETED".equalsIgnoreCase(order.getStatus())) {
                boolean productInOrder = orderItemRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())
                        .stream()
                        .anyMatch(item -> item.getProduct().getId().equals(productId));
                if (productInOrder) {
                    isVerified = true;
                }
            }
        }

        Review review = new Review(
                product,
                user,
                order,
                request.rating(),
                request.comment() != null ? request.comment().trim() : null,
                isVerified
        );

        return ReviewResponse.fromEntity(reviewRepository.save(review));
    }

    public PageResponse<ReviewResponse> getReviewsByProduct(UUID productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        Pageable sortedPageable = PageableDefaults.withDefaultSort(
                pageable,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return PageResponse.from(reviewRepository.findByProductId(productId, sortedPageable)
                .map(ReviewResponse::fromEntity));
    }

    public RatingSummaryResponse getRatingSummary(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }
        Double avgRating = reviewRepository.findAverageRatingByProductId(productId);
        long count = reviewRepository.countByProductId(productId);
        return new RatingSummaryResponse(productId, avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0, count);
    }
}
