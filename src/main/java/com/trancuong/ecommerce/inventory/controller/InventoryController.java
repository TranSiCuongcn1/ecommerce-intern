package com.trancuong.ecommerce.inventory.controller;

import com.trancuong.ecommerce.common.api.ApiResponse;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.inventory.dto.InventoryAllocationRequest;
import com.trancuong.ecommerce.inventory.dto.InventoryAllocationResponse;
import com.trancuong.ecommerce.inventory.dto.InventoryRequest;
import com.trancuong.ecommerce.inventory.dto.InventoryResponse;
import com.trancuong.ecommerce.inventory.service.InventoryService;
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
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ApiResponse<PageResponse<InventoryResponse>> getInventoryItems(
            @RequestParam(required = false) String filter,
            @ParameterObject Pageable pageable
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get inventory items successfully",
                inventoryService.findAll(filter, pageable)
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryResponse> getInventoryItem(@PathVariable UUID id) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Get inventory item successfully",
                inventoryService.findById(id)
        );
    }

    @PostMapping("/allocate")
    public ApiResponse<InventoryAllocationResponse> allocateInventory(
            @Valid @RequestBody InventoryAllocationRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Allocate inventory successfully",
                inventoryService.allocate(request)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<InventoryResponse> createInventoryItem(@Valid @RequestBody InventoryRequest request) {
        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "Create inventory item successfully",
                inventoryService.create(request)
        );
    }

    @PutMapping("/{id}")
    public ApiResponse<InventoryResponse> updateInventoryItem(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Update inventory item successfully",
                inventoryService.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteInventoryItem(@PathVariable UUID id) {
        inventoryService.delete(id);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "Delete inventory item successfully",
                null
        );
    }
}
