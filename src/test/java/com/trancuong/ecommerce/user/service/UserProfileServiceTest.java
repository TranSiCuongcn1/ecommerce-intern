package com.trancuong.ecommerce.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.domain.UserAddress;
import com.trancuong.ecommerce.user.dto.UserAddressRequest;
import com.trancuong.ecommerce.user.dto.UserAddressResponse;
import com.trancuong.ecommerce.user.dto.UserProfileResponse;
import com.trancuong.ecommerce.user.mapper.UserAddressMapper;
import com.trancuong.ecommerce.user.repository.UserAddressRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserAddressMapper userAddressMapper;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void getProfile_includesDefaultAddressWhenPresent() {
        User user = user();
        UserAddress address = address(user, true);
        UserAddressResponse addressResponse = response(address);

        when(userAddressRepository.findByUserIdAndDefaultAddressTrue(user.getId()))
                .thenReturn(Optional.of(address));
        when(userAddressMapper.toResponse(address)).thenReturn(addressResponse);

        UserProfileResponse response = userProfileService.getProfile(user);

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.role()).isEqualTo("CUSTOMER");
        assertThat(response.defaultAddress()).isEqualTo(addressResponse);
    }

    @Test
    void createAddress_whenFirstAddress_marksDefault() {
        User user = user();
        UserAddressRequest request = request(false);

        when(userAddressRepository.existsByUserId(user.getId())).thenReturn(false);
        when(userAddressRepository.save(org.mockito.ArgumentMatchers.any(UserAddress.class)))
                .thenAnswer(invocation -> {
                    UserAddress address = invocation.getArgument(0);
                    ReflectionTestUtils.setField(address, "id", UUID.randomUUID());
                    return address;
                });
        when(userAddressMapper.toResponse(org.mockito.ArgumentMatchers.any(UserAddress.class)))
                .thenAnswer(invocation -> response(invocation.getArgument(0)));

        UserAddressResponse response = userProfileService.createAddress(user, request);

        assertThat(response.defaultAddress()).isTrue();
    }

    @Test
    void setDefaultAddress_clearsExistingDefaultAndMarksAddress() {
        User user = user();
        UserAddress address = address(user, false);

        when(userAddressRepository.findByIdAndUserId(address.getId(), user.getId()))
                .thenReturn(Optional.of(address));
        when(userAddressMapper.toResponse(address)).thenAnswer(invocation -> response(invocation.getArgument(0)));

        UserAddressResponse response = userProfileService.setDefaultAddress(user, address.getId());

        assertThat(address.isDefaultAddress()).isTrue();
        assertThat(response.defaultAddress()).isTrue();
        verify(userAddressRepository).clearDefaultByUserId(user.getId());
        verify(userAddressRepository).flush();
    }

    private User user() {
        User user = new User(
                "Test User",
                "customer@example.com",
                "password-hash",
                Role.CUSTOMER
        );
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }

    private UserAddress address(User user, boolean defaultAddress) {
        UserAddress address = new UserAddress(
                user,
                "Test Customer",
                "0900000000",
                "Ho Chi Minh",
                "District 1",
                "Ben Nghe",
                "123 Nguyen Hue",
                defaultAddress
        );
        ReflectionTestUtils.setField(address, "id", UUID.randomUUID());
        return address;
    }

    private UserAddressRequest request(boolean defaultAddress) {
        return new UserAddressRequest(
                " Test Customer ",
                " 0900000000 ",
                " Ho Chi Minh ",
                " District 1 ",
                " Ben Nghe ",
                " 123 Nguyen Hue ",
                defaultAddress
        );
    }

    private UserAddressResponse response(UserAddress address) {
        return new UserAddressResponse(
                address.getId(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getProvince(),
                address.getDistrict(),
                address.getWard(),
                address.getDetailAddress(),
                address.isDefaultAddress(),
                address.getCreatedAt(),
                address.getUpdatedAt()
        );
    }
}
