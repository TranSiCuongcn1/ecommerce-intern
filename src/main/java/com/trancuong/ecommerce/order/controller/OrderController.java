package com.trancuong.ecommerce.order.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.order.dto.CheckoutRequest;
import com.trancuong.ecommerce.order.dto.OrderResponse;
import com.trancuong.ecommerce.order.service.OrderService;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal User user,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get orders successfully",
                orderService.findMyOrders(user, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getMyOrder(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get order successfully",
                orderService.findMyOrderById(user, id)
        );
    }

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
