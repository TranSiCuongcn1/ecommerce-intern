package com.trancuong.ecommerce.inventory.controller;

import com.trancuong.ecommerce.inventory.dto.InventoryRequest;
import com.trancuong.ecommerce.inventory.dto.InventoryResponse;
import com.trancuong.ecommerce.inventory.service.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<InventoryResponse> getInventoryItems(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false, defaultValue = "false") boolean availableOnly,
            @RequestParam(required = false, defaultValue = "false") boolean lowStockOnly
    ) {
        return inventoryService.findAll(productId, warehouseId, availableOnly, lowStockOnly);
    }

    @GetMapping("/{id}")
    public InventoryResponse getInventoryItem(@PathVariable UUID id) {
        return inventoryService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryResponse createInventoryItem(@Valid @RequestBody InventoryRequest request) {
        return inventoryService.create(request);
    }

    @PutMapping("/{id}")
    public InventoryResponse updateInventoryItem(
            @PathVariable UUID id,
            @Valid @RequestBody InventoryRequest request
    ) {
        return inventoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInventoryItem(@PathVariable UUID id) {
        inventoryService.delete(id);
    }
}
