package com.trancuong.ecommerce.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.category.dto.CategoryRequest;
import com.trancuong.ecommerce.category.dto.CategoryResponse;
import com.trancuong.ecommerce.category.exception.DuplicateCategorySlugException;
import com.trancuong.ecommerce.category.mapper.CategoryMapper;
import com.trancuong.ecommerce.category.repository.CategoryRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_trimsValuesAndSavesCategory() {
        Category saved = category("Phones", "phones");

        when(categoryRepository.existsBySlug("phones")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(response(saved));

        CategoryResponse response = categoryService.create(new CategoryRequest(" Phones ", " phones "));

        assertThat(response.name()).isEqualTo("Phones");
        assertThat(response.slug()).isEqualTo("phones");
    }

    @Test
    void create_whenSlugExists_throwsDuplicateSlug() {
        when(categoryRepository.existsBySlug("phones")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Phones", "phones")))
                .isInstanceOf(DuplicateCategorySlugException.class);
    }

    @Test
    void update_updatesCategoryAndFlushes() {
        Category category = category("Phones", "phones");

        when(categoryRepository.findById(category.getId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsBySlugAndIdNot("smartphones", category.getId())).thenReturn(false);
        when(categoryMapper.toResponse(category)).thenAnswer(invocation -> response(invocation.getArgument(0)));

        CategoryResponse response = categoryService.update(
                category.getId(),
                new CategoryRequest(" Smartphones ", " smartphones ")
        );

        assertThat(response.name()).isEqualTo("Smartphones");
        assertThat(response.slug()).isEqualTo("smartphones");
        verify(categoryRepository).flush();
    }

    private Category category(String name, String slug) {
        Category category = new Category(name, slug);
        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
        return category;
    }

    private CategoryResponse response(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}
