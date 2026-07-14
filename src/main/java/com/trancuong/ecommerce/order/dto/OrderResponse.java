package com.trancuong.ecommerce.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String status,
        String paymentStatus,
        String paymentMethod,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        String receiverName,
        String receiverPhone,
        String shippingAddress,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
