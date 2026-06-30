package com.trancuong.ecommerce.product.service;

import com.trancuong.ecommerce.category.domain.Category;
import com.trancuong.ecommerce.category.exception.CategoryNotFoundException;
import com.trancuong.ecommerce.category.repository.CategoryRepository;
import com.trancuong.ecommerce.common.api.PageResponse;
import com.trancuong.ecommerce.common.api.PageableDefaults;
import com.trancuong.ecommerce.common.api.RsqlSpecifications;
import com.trancuong.ecommerce.product.domain.Product;
import com.trancuong.ecommerce.product.dto.ProductRequest;
import com.trancuong.ecommerce.product.dto.ProductResponse;
import com.trancuong.ecommerce.product.dto.ProductResponse.CategorySummary;
import com.trancuong.ecommerce.product.exception.DuplicateProductSlugException;
import com.trancuong.ecommerce.product.exception.ProductNotFoundException;
import com.trancuong.ecommerce.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public PageResponse<ProductResponse> findAll(String filter, Pageable pageable) {
        Pageable sortedPageable = PageableDefaults.withDefaultSort(
                pageable,
                Sort.by(Sort.Direction.ASC, "name")
        );
        return PageResponse.from(productRepository
                .findAll(RsqlSpecifications.from(filter), sortedPageable)
                .map(this::toResponse));
    }

    public ProductResponse findById(UUID id) {
        return toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = getCategory(request.categoryId());
        String name = request.name().trim();
        String slug = request.slug().trim();
        String description = normalizeText(request.description());
        BigDecimal price = request.price();
        String imageUrl = normalizeText(request.imageUrl());
        String status = normalizeStatus(request.status());

        if (productRepository.existsBySlug(slug)) {
            throw new DuplicateProductSlugException(slug);
        }

        Product product = new Product(category, name, slug, description, price, imageUrl, status);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = getProduct(id);
        Category category = getCategory(request.categoryId());
        String name = request.name().trim();
        String slug = request.slug().trim();
        String description = normalizeText(request.description());
        BigDecimal price = request.price();
        String imageUrl = normalizeText(request.imageUrl());
        String status = normalizeStatus(request.status());

        if (productRepository.existsBySlugAndIdNot(slug, id)) {
            throw new DuplicateProductSlugException(slug);
        }

        product.update(category, name, slug, description, price, imageUrl, status);
        productRepository.flush();
        return toResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = getProduct(id);
        productRepository.delete(product);
        productRepository.flush();
    }

    private Product getProduct(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
    }

    private ProductResponse toResponse(Product product) {
        Category category = product.getCategory();
        return new ProductResponse(
                product.getId(),
                new CategorySummary(category.getId(), category.getName(), category.getSlug()),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.getImageUrl(),
                product.getStatus(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        return status.trim().toUpperCase();
    }

}
