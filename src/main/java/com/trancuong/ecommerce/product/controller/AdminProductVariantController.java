package com.trancuong.ecommerce.product.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.product.dto.CreateProductVariantRequest;
import com.trancuong.ecommerce.product.dto.ProductVariantResponse;
import com.trancuong.ecommerce.product.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Products", description = "Admin Product Variant Management APIs")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductVariantController {

    private final ProductVariantService variantService;

    @Operation(summary = "Create a new variant for a product")
    @PostMapping("/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductVariantResponse> createVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody CreateProductVariantRequest request
    ) {
        return ApiResponse.success(201, "Variant created successfully", variantService.createVariant(productId, request));
    }

    @Operation(summary = "Delete a product variant")
    @DeleteMapping("/variants/{variantId}")
    public ApiResponse<String> deleteVariant(@PathVariable UUID variantId) {
        variantService.deleteVariant(variantId);
        return ApiResponse.success(200, "Variant deleted successfully", "Variant deleted successfully");
    }
}
