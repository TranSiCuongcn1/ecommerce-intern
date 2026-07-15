package com.trancuong.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin.bootstrap")
public record AdminBootstrapProperties(
        boolean enabled,
        String fullName,
        String email,
        String password
) {
}
