package com.trancuong.ecommerce.inventory.service;

import com.trancuong.ecommerce.inventory.domain.Inventory;
import com.trancuong.ecommerce.inventory.dto.InventoryRequest;
import com.trancuong.ecommerce.inventory.dto.InventoryResponse;
import com.trancuong.ecommerce.inventory.dto.InventoryResponse.ProductSummary;
import com.trancuong.ecommerce.inventory.dto.InventoryResponse.WarehouseSummary;
import com.trancuong.ecommerce.inventory.exception.DuplicateInventoryException;
import com.trancuong.ecommerce.inventory.exception.InventoryNotFoundException;
import com.trancuong.ecommerce.inventory.repository.InventoryRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import com.trancuong.ecommerce.warehouse.exception.WarehouseNotFoundException;
import com.trancuong.ecommerce.warehouse.repository.WarehouseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;

    public InventoryService(
            InventoryRepository inventoryRepository,
            ProductRepository productRepository,
            WarehouseRepository warehouseRepository
    ) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public List<InventoryResponse> findAll() {
        return inventoryRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InventoryResponse findById(UUID id) {
        return toResponse(getInventory(id));
    }

    @Transactional
    public InventoryResponse create(InventoryRequest request) {
        Product product = getProduct(request.productId());
        Warehouse warehouse = getWarehouse(request.warehouseId());

        if (inventoryRepository.existsByProductIdAndWarehouseId(
                request.productId(),
                request.warehouseId()
        )) {
            throw new DuplicateInventoryException(request.productId(), request.warehouseId());
        }

        Inventory inventory = new Inventory(
                product,
                warehouse,
                request.quantityOnHand(),
                request.quantityReserved(),
                request.reorderLevel()
        );
        return toResponse(inventoryRepository.save(inventory));
    }

    @Transactional
    public InventoryResponse update(UUID id, InventoryRequest request) {
        Inventory inventory = getInventory(id);
        Product product = getProduct(request.productId());
        Warehouse warehouse = getWarehouse(request.warehouseId());

        if (inventoryRepository.existsByProductIdAndWarehouseIdAndIdNot(
                request.productId(),
                request.warehouseId(),
                id
        )) {
            throw new DuplicateInventoryException(request.productId(), request.warehouseId());
        }

        inventory.update(
                product,
                warehouse,
                request.quantityOnHand(),
                request.quantityReserved(),
                request.reorderLevel()
        );
        inventoryRepository.flush();
        return toResponse(inventory);
    }

    @Transactional
    public void delete(UUID id) {
        Inventory inventory = getInventory(id);
        inventoryRepository.delete(inventory);
        inventoryRepository.flush();
    }

    private Inventory getInventory(UUID id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(id));
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Warehouse getWarehouse(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }

    private InventoryResponse toResponse(Inventory inventory) {
        Product product = inventory.getProduct();
        Warehouse warehouse = inventory.getWarehouse();
        return new InventoryResponse(
                inventory.getId(),
                new ProductSummary(product.getId(), product.getName(), product.getSlug()),
                new WarehouseSummary(warehouse.getId(), warehouse.getCode(), warehouse.getName()),
                inventory.getQuantityOnHand(),
                inventory.getQuantityReserved(),
                inventory.getAvailableQuantity(),
                inventory.getReorderLevel(),
                inventory.getVersion(),
                inventory.getCreatedAt(),
                inventory.getUpdatedAt()
        );
    }
}
