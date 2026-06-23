package com.trancuong.ecommerce.warehouse.service;

import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import com.trancuong.ecommerce.warehouse.dto.WarehouseRequest;
import com.trancuong.ecommerce.warehouse.dto.WarehouseResponse;
import com.trancuong.ecommerce.warehouse.exception.DuplicateWarehouseCodeException;
import com.trancuong.ecommerce.warehouse.exception.WarehouseNotFoundException;
import com.trancuong.ecommerce.warehouse.repository.WarehouseRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    public List<WarehouseResponse> findAll(String keyword, String status) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String normalizedStatus = normalizeOptionalStatus(status);
        return warehouseRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .filter(warehouse -> matchesKeyword(
                        normalizedKeyword,
                        warehouse.getCode(),
                        warehouse.getName(),
                        warehouse.getAddress()
                ))
                .filter(warehouse -> normalizedStatus == null
                        || warehouse.getStatus().equalsIgnoreCase(normalizedStatus))
                .map(this::toResponse)
                .toList();
    }

    public WarehouseResponse findById(UUID id) {
        return toResponse(getWarehouse(id));
    }

    @Transactional
    public WarehouseResponse create(WarehouseRequest request) {
        String code = normalizeCode(request.code());
        String name = request.name().trim();
        String address = normalizeAddress(request.address());
        String status = normalizeStatus(request.status());

        if (warehouseRepository.existsByCode(code)) {
            throw new DuplicateWarehouseCodeException(code);
        }

        return toResponse(warehouseRepository.save(new Warehouse(code, name, address, status)));
    }

    @Transactional
    public WarehouseResponse update(UUID id, WarehouseRequest request) {
        Warehouse warehouse = getWarehouse(id);
        String code = normalizeCode(request.code());
        String name = request.name().trim();
        String address = normalizeAddress(request.address());
        String status = normalizeStatus(request.status());

        if (warehouseRepository.existsByCodeAndIdNot(code, id)) {
            throw new DuplicateWarehouseCodeException(code);
        }

        warehouse.update(code, name, address, status);
        warehouseRepository.flush();
        return toResponse(warehouse);
    }

    @Transactional
    public void delete(UUID id) {
        Warehouse warehouse = getWarehouse(id);
        warehouseRepository.delete(warehouse);
        warehouseRepository.flush();
    }

    private Warehouse getWarehouse(UUID id) {
        return warehouseRepository.findById(id)
                .orElseThrow(() -> new WarehouseNotFoundException(id));
    }

    private WarehouseResponse toResponse(Warehouse warehouse) {
        return new WarehouseResponse(
                warehouse.getId(),
                warehouse.getCode(),
                warehouse.getName(),
                warehouse.getAddress(),
                warehouse.getStatus(),
                warehouse.getCreatedAt(),
                warehouse.getUpdatedAt()
        );
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }

    private String normalizeStatus(String status) {
        return status.trim().toUpperCase();
    }

    private String normalizeOptionalStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return normalizeStatus(status);
    }

    private String normalizeAddress(String address) {
        if (address == null || address.isBlank()) {
            return null;
        }
        return address.trim();
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim().toLowerCase();
    }

    private boolean matchesKeyword(String keyword, String... values) {
        if (keyword == null) {
            return true;
        }

        for (String value : values) {
            if (value != null && value.toLowerCase().contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
