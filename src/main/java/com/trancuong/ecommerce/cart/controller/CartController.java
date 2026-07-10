package com.trancuong.ecommerce.cart.controller;

import com.trancuong.ecommerce.cart.dto.CartItemQuantityRequest;
import com.trancuong.ecommerce.cart.dto.CartItemRequest;
import com.trancuong.ecommerce.cart.dto.CartResponse;
import com.trancuong.ecommerce.cart.service.CartService;
import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<CartResponse> getCart(@AuthenticationPrincipal User user) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get cart successfully",
                cartService.getCart(user)
        );
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CartResponse> addItem(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CartItemRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Add cart item successfully",
                cartService.addItem(user, request)
        );
    }

    @PutMapping("/items/{id}")
    public ApiResponse<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id,
            @Valid @RequestBody CartItemQuantityRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Update cart item successfully",
                cartService.updateItemQuantity(user, id, request)
        );
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable UUID id
    ) {
        cartService.removeItem(user, id);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Remove cart item successfully",
                null
        );
    }

    @DeleteMapping
    public ApiResponse<Void> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Clear cart successfully",
                null
        );
    }
}
