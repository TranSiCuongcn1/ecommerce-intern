package com.trancuong.ecommerce.warehouse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.warehouse.domain.Warehouse;
import com.trancuong.ecommerce.warehouse.dto.WarehouseRequest;
import com.trancuong.ecommerce.warehouse.dto.WarehouseResponse;
import com.trancuong.ecommerce.warehouse.exception.DuplicateWarehouseCodeException;
import com.trancuong.ecommerce.warehouse.mapper.WarehouseMapper;
import com.trancuong.ecommerce.warehouse.repository.WarehouseRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseMapper warehouseMapper;

    @InjectMocks
    private WarehouseService warehouseService;

    @Test
    void create_normalizesCodeStatusAndBlankAddress() {
        Warehouse saved = warehouse("HCM-01", "Ho Chi Minh Warehouse", null, "ACTIVE");

        when(warehouseRepository.existsByCode("HCM-01")).thenReturn(false);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(saved);
        when(warehouseMapper.toResponse(saved)).thenReturn(response(saved));

        WarehouseResponse response = warehouseService.create(new WarehouseRequest(
                " hcm-01 ",
                " Ho Chi Minh Warehouse ",
                " ",
                " active "
        ));

        assertThat(response.code()).isEqualTo("HCM-01");
        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(response.address()).isNull();
    }

    @Test
    void create_whenCodeExists_throwsDuplicateCode() {
        when(warehouseRepository.existsByCode("HCM-01")).thenReturn(true);

        assertThatThrownBy(() -> warehouseService.create(new WarehouseRequest(
                "hcm-01",
                "Ho Chi Minh Warehouse",
                "District 1",
                "ACTIVE"
        ))).isInstanceOf(DuplicateWarehouseCodeException.class);
    }

    @Test
    void update_updatesWarehouseAndFlushes() {
        Warehouse warehouse = warehouse("HCM-01", "Old", "Old address", "ACTIVE");

        when(warehouseRepository.findById(warehouse.getId())).thenReturn(Optional.of(warehouse));
        when(warehouseRepository.existsByCodeAndIdNot("HN-01", warehouse.getId())).thenReturn(false);
        when(warehouseMapper.toResponse(warehouse)).thenAnswer(invocation -> response(invocation.getArgument(0)));

        WarehouseResponse response = warehouseService.update(
                warehouse.getId(),
                new WarehouseRequest(" hn-01 ", " Hanoi Warehouse ", " Cau Giay ", " inactive ")
        );

        assertThat(response.code()).isEqualTo("HN-01");
        assertThat(response.name()).isEqualTo("Hanoi Warehouse");
        assertThat(response.status()).isEqualTo("INACTIVE");
        verify(warehouseRepository).flush();
    }

    private Warehouse warehouse(String code, String name, String address, String status) {
        Warehouse warehouse = new Warehouse(code, name, address, status);
        ReflectionTestUtils.setField(warehouse, "id", UUID.randomUUID());
        return warehouse;
    }

    private WarehouseResponse response(Warehouse warehouse) {
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
}
