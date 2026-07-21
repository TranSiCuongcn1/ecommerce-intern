package com.trancuong.ecommerce.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.momo")
public record MomoProperties(
        String partnerCode,
        String accessKey,
        String secretKey,
        String apiUrl,
        String redirectUrl,
        String ipnUrl
) {
}
