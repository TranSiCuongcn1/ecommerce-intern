package com.trancuong.ecommerce.category.exception;

public class DuplicateCategorySlugException extends RuntimeException {

    public DuplicateCategorySlugException(String slug) {
        super("Category slug already exists: " + slug);
    }
}
