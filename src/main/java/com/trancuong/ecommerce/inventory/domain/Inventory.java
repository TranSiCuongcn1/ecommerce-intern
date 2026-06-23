package com.trancuong.ecommerce.inventory.domain;

import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "inventory")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Inventory() {
    }

    public Inventory(
            Product product,
            Warehouse warehouse,
            Integer quantityOnHand,
            Integer quantityReserved,
            Integer reorderLevel
    ) {
        this.product = product;
        this.warehouse = warehouse;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
        this.reorderLevel = reorderLevel;
    }

    public void update(
            Product product,
            Warehouse warehouse,
            Integer quantityOnHand,
            Integer quantityReserved,
            Integer reorderLevel
    ) {
        this.product = product;
        this.warehouse = warehouse;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = quantityReserved;
        this.reorderLevel = reorderLevel;
    }

    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }
}
