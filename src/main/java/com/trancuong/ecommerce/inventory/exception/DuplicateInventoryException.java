package com.trancuong.ecommerce.inventory.exception;

import java.util.UUID;

public class DuplicateInventoryException extends RuntimeException {

    public DuplicateInventoryException(UUID productId, UUID warehouseId) {
        super("Inventory already exists for product " + productId + " in warehouse " + warehouseId);
    }
}
