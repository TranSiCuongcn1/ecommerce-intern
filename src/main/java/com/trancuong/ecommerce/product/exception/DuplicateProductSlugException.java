package com.trancuong.ecommerce.product.exception;

public class DuplicateProductSlugException extends RuntimeException {

    public DuplicateProductSlugException(String slug) {
        super("Product slug already exists: " + slug);
    }
}
