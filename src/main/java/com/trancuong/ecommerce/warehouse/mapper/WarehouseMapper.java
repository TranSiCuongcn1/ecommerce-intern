package com.trancuong.ecommerce.warehouse.mapper;

import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import com.trancuong.ecommerce.warehouse.dto.WarehouseResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WarehouseMapper {

    WarehouseResponse toResponse(Warehouse warehouse);

    List<WarehouseResponse> toResponses(List<Warehouse> warehouses);
}
