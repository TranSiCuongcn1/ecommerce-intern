package com.trancuong.ecommerce.inventory.repository;

import com.trancuong.ecommerce.inventory.domain.Inventory;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    boolean existsByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    boolean existsByProductIdAndWarehouseIdAndIdNot(UUID productId, UUID warehouseId, UUID id);
}
