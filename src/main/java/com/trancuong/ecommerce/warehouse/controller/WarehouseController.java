package com.trancuong.ecommerce.warehouse.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.warehouse.dto.WarehouseRequest;
import com.trancuong.ecommerce.warehouse.dto.WarehouseResponse;
import com.trancuong.ecommerce.warehouse.service.WarehouseService;
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
@RequestMapping("/api/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ApiResponse<PageResponse<WarehouseResponse>> getWarehouses(
            @RequestParam(required = false) String filter,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get warehouses successfully",
                warehouseService.findAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<WarehouseResponse> getWarehouse(@PathVariable UUID id) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get warehouse successfully",
                warehouseService.findById(id)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<WarehouseResponse> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Create warehouse successfully",
                warehouseService.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<WarehouseResponse> updateWarehouse(
            @PathVariable UUID id,
            @Valid @RequestBody WarehouseRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Update warehouse successfully",
                warehouseService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteWarehouse(@PathVariable UUID id) {
        warehouseService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Delete warehouse successfully",
                null
        );
    }
}
