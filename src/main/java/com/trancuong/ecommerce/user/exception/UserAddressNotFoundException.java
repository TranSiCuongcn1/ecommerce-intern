package com.trancuong.ecommerce.user.exception;

import java.util.UUID;

public class UserAddressNotFoundException extends RuntimeException {

    public UserAddressNotFoundException(UUID id) {
        super("User address not found: " + id);
    }
}
