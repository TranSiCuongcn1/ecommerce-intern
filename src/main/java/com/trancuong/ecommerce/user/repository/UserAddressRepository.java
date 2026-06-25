package com.trancuong.ecommerce.user.repository;

import com.trancuong.ecommerce.user.domain.UserAddress;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByUserIdOrderByDefaultAddressDescCreatedAtDesc(UUID userId);

    Optional<UserAddress> findByUserIdAndDefaultAddressTrue(UUID userId);

    Optional<UserAddress> findByIdAndUserId(UUID id, UUID userId);

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("update UserAddress address set address.defaultAddress = false where address.user.id = :userId")
    void clearDefaultByUserId(@Param("userId") UUID userId);
}
