package com.trancuong.ecommerce.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        AdminBootstrapProperties.class,
        MinioProperties.class
})
public class ApplicationPropertiesConfig {
}
