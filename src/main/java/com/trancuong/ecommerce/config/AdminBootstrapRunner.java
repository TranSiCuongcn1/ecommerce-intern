package com.trancuong.ecommerce.config;

import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {

    private final AdminBootstrapProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!properties.enabled()) {
            return;
        }

        String email = properties.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }

        userRepository.save(new User(
                properties.fullName().trim(),
                email,
                passwordEncoder.encode(properties.password()),
                Role.ADMIN
        ));
    }
}
