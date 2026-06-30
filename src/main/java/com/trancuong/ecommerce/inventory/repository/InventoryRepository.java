package com.trancuong.ecommerce.inventory.repository;

import com.trancuong.ecommerce.inventory.domain.Inventory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface InventoryRepository extends JpaRepository<Inventory, UUID>, JpaSpecificationExecutor<Inventory> {

    List<Inventory> findByProductId(UUID productId);

    boolean existsByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    boolean existsByProductIdAndWarehouseIdAndIdNot(UUID productId, UUID warehouseId, UUID id);
}
