package com.trancuong.ecommerce.category.service;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.category.dto.CategoryRequest;
import com.trancuong.ecommerce.category.dto.CategoryResponse;
import com.trancuong.ecommerce.category.exception.CategoryNotFoundException;
import com.trancuong.ecommerce.category.exception.DuplicateCategorySlugException;
import com.trancuong.ecommerce.category.mapper.CategoryMapper;
import com.trancuong.ecommerce.category.repository.CategoryRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"))
                .stream()
                .filter(category -> matchesKeyword(
                        normalizedKeyword,
                        category.getName(),
                        category.getSlug()
                ))
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(UUID id) {
        return categoryMapper.toResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String name = request.name().trim();
        String slug = request.slug().trim();
        if (categoryRepository.existsBySlug(slug)) {
            throw new DuplicateCategorySlugException(slug);
        }

        return categoryMapper.toResponse(categoryRepository.save(new Category(name, slug)));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = getCategory(id);
        String name = request.name().trim();
        String slug = request.slug().trim();
        if (categoryRepository.existsBySlugAndIdNot(slug, id)) {
            throw new DuplicateCategorySlugException(slug);
        }

        category.update(name, slug);
        categoryRepository.flush();
        return categoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = getCategory(id);
        categoryRepository.delete(category);
        categoryRepository.flush();
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
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
