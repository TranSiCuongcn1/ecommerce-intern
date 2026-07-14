package com.trancuong.ecommerce.order.exception;

public class CheckoutAddressNotFoundException extends RuntimeException {

    public CheckoutAddressNotFoundException() {
        super("Checkout address not found");
    }
}
