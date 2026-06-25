package com.trancuong.ecommerce.user.domain;

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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_addresses")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "receiver_name", nullable = false, length = 150)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String ward;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserAddress() {
    }

    public UserAddress(
            User user,
            String receiverName,
            String receiverPhone,
            String province,
            String district,
            String ward,
            String detailAddress,
            boolean defaultAddress
    ) {
        this.user = user;
        update(receiverName, receiverPhone, province, district, ward, detailAddress, defaultAddress);
    }

    public void update(
            String receiverName,
            String receiverPhone,
            String province,
            String district,
            String ward,
            String detailAddress,
            boolean defaultAddress
    ) {
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detailAddress = detailAddress;
        this.defaultAddress = defaultAddress;
    }

    public void markDefault() {
        this.defaultAddress = true;
    }
}
