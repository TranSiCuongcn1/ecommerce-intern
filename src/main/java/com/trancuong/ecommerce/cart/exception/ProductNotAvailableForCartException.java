package com.trancuong.ecommerce.cart.exception;

import java.util.UUID;

public class ProductNotAvailableForCartException extends RuntimeException {

    public ProductNotAvailableForCartException(UUID id) {
        super("Product is not available for cart with id: " + id);
    }
}
