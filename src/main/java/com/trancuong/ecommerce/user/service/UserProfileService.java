package com.trancuong.ecommerce.user.service;

import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.domain.UserAddress;
import com.trancuong.ecommerce.user.dto.UserAddressRequest;
import com.trancuong.ecommerce.user.dto.UserAddressResponse;
import com.trancuong.ecommerce.user.dto.UserProfileResponse;
import com.trancuong.ecommerce.user.exception.UserAddressNotFoundException;
import com.trancuong.ecommerce.user.repository.UserAddressRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileService {

    private final UserAddressRepository userAddressRepository;

    public UserProfileResponse getProfile(User user) {
        UserAddressResponse defaultAddress = userAddressRepository
                .findByUserIdAndDefaultAddressTrue(user.getId())
                .map(this::toAddressResponse)
                .orElse(null);

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                defaultAddress,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public List<UserAddressResponse> getAddresses(User user) {
        return userAddressRepository.findByUserIdOrderByDefaultAddressDescCreatedAtDesc(user.getId())
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Transactional
    public UserAddressResponse createAddress(User user, UserAddressRequest request) {
        boolean defaultAddress = request.defaultAddress() || !userAddressRepository.existsByUserId(user.getId());
        if (defaultAddress) {
            userAddressRepository.clearDefaultByUserId(user.getId());
        }

        UserAddress address = new UserAddress(
                user,
                request.receiverName().trim(),
                request.receiverPhone().trim(),
                request.province().trim(),
                request.district().trim(),
                request.ward().trim(),
                request.detailAddress().trim(),
                defaultAddress
        );

        return toAddressResponse(userAddressRepository.save(address));
    }

    @Transactional
    public UserAddressResponse updateAddress(User user, UUID id, UserAddressRequest request) {
        UserAddress address = getAddress(user, id);
        if (request.defaultAddress() && !address.isDefaultAddress()) {
            userAddressRepository.clearDefaultByUserId(user.getId());
        }

        address.update(
                request.receiverName().trim(),
                request.receiverPhone().trim(),
                request.province().trim(),
                request.district().trim(),
                request.ward().trim(),
                request.detailAddress().trim(),
                request.defaultAddress()
        );
        userAddressRepository.flush();
        return toAddressResponse(address);
    }

    @Transactional
    public void deleteAddress(User user, UUID id) {
        UserAddress address = getAddress(user, id);
        userAddressRepository.delete(address);
        userAddressRepository.flush();
    }

    @Transactional
    public UserAddressResponse setDefaultAddress(User user, UUID id) {
        UserAddress address = getAddress(user, id);
        if (!address.isDefaultAddress()) {
            userAddressRepository.clearDefaultByUserId(user.getId());
            address.markDefault();
            userAddressRepository.flush();
        }
        return toAddressResponse(address);
    }

    private UserAddress getAddress(User user, UUID id) {
        return userAddressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new UserAddressNotFoundException(id));
    }

    private UserAddressResponse toAddressResponse(UserAddress address) {
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
