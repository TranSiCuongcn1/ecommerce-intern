package com.trancuong.ecommerce.cart.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @GetMapping
    public ApiResponse<Map<String, String>> getCart() {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get cart successfully",
                Map.of("message", "TODO: return current customer cart")
        );
    }
}
