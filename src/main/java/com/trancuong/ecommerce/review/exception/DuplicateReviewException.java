package com.trancuong.ecommerce.review.exception;

public class DuplicateReviewException extends RuntimeException {
    public DuplicateReviewException() {
        super("You have already reviewed this product.");
    }
}
