package com.trancuong.ecommerce.warehouse.controller;

import com.trancuong.ecommerce.warehouse.dto.WarehouseRequest;
import com.trancuong.ecommerce.warehouse.dto.WarehouseResponse;
import com.trancuong.ecommerce.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    public List<WarehouseResponse> getWarehouses(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status
    ) {
        return warehouseService.findAll(keyword, status);
    }

    @GetMapping("/{id}")
    public WarehouseResponse getWarehouse(@PathVariable UUID id) {
        return warehouseService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WarehouseResponse createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return warehouseService.create(request);
    }

    @PutMapping("/{id}")
    public WarehouseResponse updateWarehouse(
            @PathVariable UUID id,
            @Valid @RequestBody WarehouseRequest request
    ) {
        return warehouseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWarehouse(@PathVariable UUID id) {
        warehouseService.delete(id);
    }
}
