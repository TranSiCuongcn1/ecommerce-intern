package com.trancuong.ecommerce.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.order.repository.OrderItemRepository;
import com.trancuong.ecommerce.order.repository.OrderRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.review.domain.Review;
import com.trancuong.ecommerce.review.dto.CreateReviewRequest;
import com.trancuong.ecommerce.review.dto.RatingSummaryResponse;
import com.trancuong.ecommerce.review.dto.ReviewResponse;
import com.trancuong.ecommerce.review.exception.DuplicateReviewException;
import com.trancuong.ecommerce.review.repository.ReviewRepository;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_createsAndReturnsReview() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        UUID productId = UUID.randomUUID();
        Product product = product(productId);

        CreateReviewRequest request = new CreateReviewRequest(5, "Great product!", null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);
        when(reviewRepository.save(any(Review.class))).thenAnswer(inv -> {
            Review r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "id", UUID.randomUUID());
            return r;
        });

        ReviewResponse response = reviewService.createReview(user, productId, request);

        assertThat(response.rating()).isEqualTo(5);
        assertThat(response.comment()).isEqualTo("Great product!");
    }

    @Test
    void createReview_whenAlreadyReviewed_throwsDuplicateReviewException() {
        User user = new User("John", "john@example.com", "pass", Role.CUSTOMER);
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(user, "id", userId);

        UUID productId = UUID.randomUUID();
        Product product = product(productId);

        CreateReviewRequest request = new CreateReviewRequest(4, "Nice", null);

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(reviewRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.createReview(user, productId, request))
                .isInstanceOf(DuplicateReviewException.class);
    }

    @Test
    void getRatingSummary_calculatesAverage() {
        UUID productId = UUID.randomUUID();
        when(productRepository.existsById(productId)).thenReturn(true);
        when(reviewRepository.findAverageRatingByProductId(productId)).thenReturn(4.66);
        when(reviewRepository.countByProductId(productId)).thenReturn(3L);

        RatingSummaryResponse summary = reviewService.getRatingSummary(productId);

        assertThat(summary.averageRating()).isEqualTo(4.7);
        assertThat(summary.totalReviews()).isEqualTo(3L);
    }

    private Product product(UUID id) {
        Category category = new Category("Phones", "phones");
        Product product = new Product(category, "iPhone 15", "iphone-15", "desc", new BigDecimal("20000000"), "img", "ACTIVE");
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }
}
