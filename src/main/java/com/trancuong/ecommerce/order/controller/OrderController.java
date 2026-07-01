package com.trancuong.ecommerce.order.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping("/checkout")
    public ApiResponse<Map<String, String>> checkout() {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Checkout successfully",
                Map.of("message", "TODO: checkout cart and create order")
        );
    }
}
