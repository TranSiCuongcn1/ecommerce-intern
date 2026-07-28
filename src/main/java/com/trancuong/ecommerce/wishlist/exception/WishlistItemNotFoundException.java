package com.trancuong.ecommerce.wishlist.exception;

public class WishlistItemNotFoundException extends RuntimeException {
    public WishlistItemNotFoundException() {
        super("Product is not in your wishlist.");
    }
}
