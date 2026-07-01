package com.trancuong.ecommerce.product.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.product.dto.ProductRequest;
import com.trancuong.ecommerce.product.dto.ProductResponse;
import com.trancuong.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String filter,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get products successfully",
                productService.findAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable UUID id) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get product successfully",
                productService.findById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Create product successfully",
                productService.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable UUID id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Update product successfully",
                productService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Delete product successfully",
                null
        );
    }
}
