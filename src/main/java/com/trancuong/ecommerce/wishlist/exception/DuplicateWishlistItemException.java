package com.trancuong.ecommerce.wishlist.exception;

public class DuplicateWishlistItemException extends RuntimeException {
    public DuplicateWishlistItemException() {
        super("Product is already in your wishlist.");
    }
}
