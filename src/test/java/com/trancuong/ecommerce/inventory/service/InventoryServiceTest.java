package com.trancuong.ecommerce.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.inventory.domain.Inventory;
import com.trancuong.ecommerce.inventory.dto.InventoryAllocationRequest;
import com.trancuong.ecommerce.inventory.dto.InventoryAllocationResponse;
import com.trancuong.ecommerce.inventory.dto.InventoryRequest;
import com.trancuong.ecommerce.inventory.dto.InventoryResponse;
import com.trancuong.ecommerce.inventory.exception.DuplicateInventoryException;
import com.trancuong.ecommerce.inventory.exception.InsufficientInventoryException;
import com.trancuong.ecommerce.inventory.repository.InventoryRepository;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import com.trancuong.ecommerce.warehouse.repository.WarehouseRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void allocate_picksActiveWarehouseWithSmallestEnoughAvailableQuantity() {
        Product product = product();
        Warehouse largeWarehouse = warehouse("HCM-01", "ACTIVE");
        Warehouse smallWarehouse = warehouse("HN-01", "ACTIVE");
        Inventory largeInventory = inventory(product, largeWarehouse, 100, 0);
        Inventory smallInventory = inventory(product, smallWarehouse, 10, 0);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(product.getId()))
                .thenReturn(List.of(largeInventory, smallInventory));

        InventoryAllocationResponse response = inventoryService.allocate(
                new InventoryAllocationRequest(product.getId(), 5)
        );

        assertThat(response.inventoryId()).isEqualTo(smallInventory.getId());
        assertThat(response.quantityReserved()).isEqualTo(5);
        assertThat(smallInventory.getQuantityReserved()).isEqualTo(5);
        assertThat(largeInventory.getQuantityReserved()).isZero();
        verify(inventoryRepository).flush();
    }

    @Test
    void allocate_whenNoWarehouseHasEnoughStock_throwsInsufficientInventory() {
        Product product = product();
        Inventory inventory = inventory(product, warehouse("HCM-01", "ACTIVE"), 3, 0);

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProductId(product.getId())).thenReturn(List.of(inventory));

        assertThatThrownBy(() -> inventoryService.allocate(
                new InventoryAllocationRequest(product.getId(), 5)
        )).isInstanceOf(InsufficientInventoryException.class);
    }

    @Test
    void create_whenProductWarehouseAlreadyExists_throwsDuplicateInventory() {
        Product product = product();
        Warehouse warehouse = warehouse("HCM-01", "ACTIVE");

        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.existsByProductIdAndWarehouseId(product.getId(), warehouse.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> inventoryService.create(new InventoryRequest(
                product.getId(),
                warehouse.getId(),
                100,
                0,
                10
        ))).isInstanceOf(DuplicateInventoryException.class);
    }

    @Test
    void update_updatesInventoryAndFlushes() {
        Product product = product();
        Warehouse warehouse = warehouse("HCM-01", "ACTIVE");
        Inventory inventory = inventory(product, warehouse, 100, 0);

        when(inventoryRepository.findById(inventory.getId())).thenReturn(Optional.of(inventory));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.existsByProductIdAndWarehouseIdAndIdNot(
                product.getId(),
                warehouse.getId(),
                inventory.getId()
        )).thenReturn(false);

        InventoryResponse response = inventoryService.update(inventory.getId(), new InventoryRequest(
                product.getId(),
                warehouse.getId(),
                80,
                5,
                20
        ));

        assertThat(response.quantityOnHand()).isEqualTo(80);
        assertThat(response.quantityReserved()).isEqualTo(5);
        assertThat(response.availableQuantity()).isEqualTo(75);
        assertThat(response.reorderLevel()).isEqualTo(20);
        verify(inventoryRepository).flush();
    }

    private Product product() {
        Category category = new Category("Phones", "phones");
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());

        Product product = new Product(
                category,
                "iPhone 15",
                "iphone-15",
                "Apple smartphone",
                new BigDecimal("19990000.00"),
                "https://example.com/iphone-15.jpg",
                "ACTIVE"
        );
        ReflectionTestUtils.setField(product, "id", UUID.randomUUID());
        return product;
    }

    private Warehouse warehouse(String code, String status) {
        Warehouse warehouse = new Warehouse(code, code + " Warehouse", "Address", status);
        ReflectionTestUtils.setField(warehouse, "id", UUID.randomUUID());
        return warehouse;
    }

    private Inventory inventory(Product product, Warehouse warehouse, int quantityOnHand, int quantityReserved) {
        Inventory inventory = new Inventory(product, warehouse, quantityOnHand, quantityReserved, 10);
        ReflectionTestUtils.setField(inventory, "id", UUID.randomUUID());
        return inventory;
    }
}
