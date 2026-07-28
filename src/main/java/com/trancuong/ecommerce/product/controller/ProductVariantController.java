package com.trancuong.ecommerce.product.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.product.dto.ProductVariantResponse;
import com.trancuong.ecommerce.product.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Public Product & Variant APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService variantService;

    @Operation(summary = "Get all variants of a product")
    @GetMapping("/{productId}/variants")
    public ApiResponse<List<ProductVariantResponse>> getVariantsByProduct(@PathVariable UUID productId) {
        return ApiResponse.success(variantService.getVariantsByProduct(productId));
    }
}
