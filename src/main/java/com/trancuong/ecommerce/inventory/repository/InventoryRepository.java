package com.trancuong.ecommerce.inventory.repository;

import com.trancuong.ecommerce.inventory.domain.Inventory;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InventoryRepository extends JpaRepository<Inventory, UUID>, JpaSpecificationExecutor<Inventory> {

    List<Inventory> findByProductId(UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select inventory from Inventory inventory where inventory.product.id = :productId")
    List<Inventory> findByProductIdForUpdate(@Param("productId") UUID productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select inventory from Inventory inventory
            where inventory.product.id = :productId and inventory.warehouse.id = :warehouseId
            """)
    Optional<Inventory> findByProductIdAndWarehouseIdForUpdate(
            @Param("productId") UUID productId,
            @Param("warehouseId") UUID warehouseId
    );

    boolean existsByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    boolean existsByProductIdAndWarehouseIdAndIdNot(UUID productId, UUID warehouseId, UUID id);
}
