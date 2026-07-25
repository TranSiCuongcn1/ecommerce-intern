package com.trancuong.ecommerce.voucher.domain;

import com.trancuong.ecommerce.order.domain.Order;
import com.trancuong.ecommerce.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Entity
@Table(name = "voucher_usages")
@Getter
public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "voucher_id", nullable = false)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;

    protected VoucherUsage() {
    }

    public VoucherUsage(Voucher voucher, User user, Order order) {
        this.voucher = voucher;
        this.user = user;
        this.order = order;
        this.usedAt = LocalDateTime.now();
    }
}
