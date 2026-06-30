package com.trancuong.ecommerce.common.api;

import io.github.perplexhub.rsql.RSQLJPASupport;
import org.springframework.data.jpa.domain.Specification;

public final class RsqlSpecifications {

    private RsqlSpecifications() {
    }

    public static <T> Specification<T> from(String filter) {
        if (filter == null || filter.isBlank()) {
            return Specification.where(null);
        }
        return RSQLJPASupport.toSpecification(filter);
    }
}
