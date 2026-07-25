package com.trancuong.ecommerce.voucher.exception;

public class DuplicateVoucherCodeException extends RuntimeException {
    public DuplicateVoucherCodeException(String code) {
        super("Voucher code already exists: " + code);
    }
}
