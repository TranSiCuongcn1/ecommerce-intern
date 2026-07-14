package com.trancuong.ecommerce.order.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.order.dto.CheckoutRequest;
import com.trancuong.ecommerce.order.dto.OrderResponse;
import com.trancuong.ecommerce.order.service.OrderService;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/checkout")
    public ApiResponse<OrderResponse> checkout(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody(required = false) CheckoutRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Checkout successfully",
                orderService.checkout(
                        user,
                        request == null ? new CheckoutRequest(null, null, null) : request
                )
        );
    }
}
