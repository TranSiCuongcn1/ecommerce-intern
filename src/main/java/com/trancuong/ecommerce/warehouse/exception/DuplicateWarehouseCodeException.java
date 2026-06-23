package com.trancuong.ecommerce.warehouse.exception;

public class DuplicateWarehouseCodeException extends RuntimeException {

    public DuplicateWarehouseCodeException(String code) {
        super("Warehouse code already exists: " + code);
    }
}
