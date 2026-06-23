package com.trancuong.ecommerce.inventory.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record InventoryRequest(
        @NotNull UUID productId,
        @NotNull UUID warehouseId,
        @NotNull @Min(0) Integer quantityOnHand,
        @NotNull @Min(0) Integer quantityReserved,
        @NotNull @Min(0) Integer reorderLevel
) {

    @AssertTrue(message = "quantityReserved must be less than or equal to quantityOnHand")
    public boolean isReservedNotGreaterThanOnHand() {
        if (quantityOnHand == null || quantityReserved == null) {
            return true;
        }
        return quantityReserved <= quantityOnHand;
    }
}
