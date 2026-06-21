package com.trancuong.ecommerce.cart;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @GetMapping
    public Map<String, String> getCart() {
        return Map.of("message", "TODO: return current customer cart");
    }
}
