package com.trancuong.ecommerce.category;

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @GetMapping
    public List<Map<String, Object>> getCategories() {
        return List.of();
    }
}
