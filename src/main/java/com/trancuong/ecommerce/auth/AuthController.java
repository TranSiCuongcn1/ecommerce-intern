package com.trancuong.ecommerce.auth;

import com.trancuong.ecommerce.auth.dto.LoginRequest;
import com.trancuong.ecommerce.auth.dto.RegisterRequest;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody RegisterRequest request) {
        return Map.of("message", "TODO: create user " + request.email());
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        return Map.of("message", "TODO: authenticate " + request.email());
    }
}
