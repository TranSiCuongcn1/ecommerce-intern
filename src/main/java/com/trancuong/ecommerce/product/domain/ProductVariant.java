package com.trancuong.ecommerce.product.domain;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "product_variants")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(precision = 15, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "attributes_summary", length = 255)
    private String attributesSummary;

    @Column(nullable = false, length = 30)
    private String status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProductVariant() {
    }

    public ProductVariant(
            Product product,
            String sku,
            BigDecimal price,
            String imageUrl,
            String attributesSummary,
            String status
    ) {
        this.product = product;
        this.sku = sku.trim().toUpperCase();
        this.price = price;
        this.imageUrl = imageUrl;
        this.attributesSummary = attributesSummary;
        this.status = status != null ? status.trim().toUpperCase() : "ACTIVE";
    }

    public BigDecimal getEffectivePrice() {
        return (price != null && price.compareTo(BigDecimal.ZERO) > 0) ? price : product.getPrice();
    }

    public void update(BigDecimal price, String imageUrl, String attributesSummary, String status) {
        this.price = price;
        this.imageUrl = imageUrl;
        this.attributesSummary = attributesSummary;
        if (status != null && !status.isBlank()) {
            this.status = status.trim().toUpperCase();
        }
    }
}
