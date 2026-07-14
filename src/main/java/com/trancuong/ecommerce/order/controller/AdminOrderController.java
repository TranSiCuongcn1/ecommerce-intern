package com.trancuong.ecommerce.order.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.order.dto.OrderResponse;
import com.trancuong.ecommerce.order.dto.OrderStatusUpdateRequest;
import com.trancuong.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderResponse>> getOrders(
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get orders successfully",
                orderService.findAllOrders(pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrder(@PathVariable UUID id) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get order successfully",
                orderService.findOrderById(id)
        );
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Update order status successfully",
                orderService.updateStatus(id, request)
        );
    }
}
