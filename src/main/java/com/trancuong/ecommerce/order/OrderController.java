package com.trancuong.ecommerce.order;

import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @PostMapping("/checkout")
    public Map<String, String> checkout() {
        return Map.of("message", "TODO: checkout cart and create order");
    }
}
