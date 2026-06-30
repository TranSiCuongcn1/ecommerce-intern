package com.trancuong.ecommerce.user.mapper;

import com.trancuong.ecommerce.user.domain.UserAddress;
import com.trancuong.ecommerce.user.dto.UserAddressResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface UserAddressMapper {

    UserAddressResponse toResponse(UserAddress address);

    List<UserAddressResponse> toResponses(List<UserAddress> addresses);
}
