package com.trancuong.ecommerce.category.mapper;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.category.dto.CategoryResponse;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CategoryMapper {

    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponses(List<Category> categories);
}
