package com.trancuong.ecommerce.common.api;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageableDefaults {

    private PageableDefaults() {
    }

    public static Pageable withDefaultSort(Pageable pageable, Sort defaultSort) {
        if (pageable.isUnpaged()) {
            return PageRequest.of(0, 20, defaultSort);
        }
        if (pageable.getSort().isSorted()) {
            return pageable;
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), defaultSort);
    }
}
