package com.trancuong.ecommerce.inventory.exception;

import java.util.UUID;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(UUID productId, Integer quantity) {
        super("No active warehouse has enough available inventory for product "
                + productId + " and quantity " + quantity);
    }
}
