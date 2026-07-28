package com.trancuong.ecommerce.common.lock;

public class LockAcquisitionException extends RuntimeException {
    public LockAcquisitionException(String message) {
        super(message);
    }
}
