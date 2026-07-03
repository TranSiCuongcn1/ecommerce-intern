package com.trancuong.ecommerce.auth.service;

import com.trancuong.ecommerce.auth.dto.AuthResponse;
import com.trancuong.ecommerce.auth.dto.AuthResponse.UserSummary;
import com.trancuong.ecommerce.auth.dto.LoginRequest;
import com.trancuong.ecommerce.auth.dto.LogoutRequest;
import com.trancuong.ecommerce.auth.dto.RefreshTokenRequest;
import com.trancuong.ecommerce.auth.dto.RegisterRequest;
import com.trancuong.ecommerce.auth.exception.DuplicateEmailException;
import com.trancuong.ecommerce.auth.exception.InvalidCredentialsException;
import com.trancuong.ecommerce.auth.exception.InvalidRefreshTokenException;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.repository.UserRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateEmailException(email);
        }

        User user = userRepository.save(new User(
                request.fullName().trim(),
                email,
                passwordEncoder.encode(request.password()),
                Role.CUSTOMER
        ));
        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken().trim();
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!Objects.equals(user.getCurrentRefreshTokenId(), jwtService.extractTokenId(refreshToken))) {
            throw new InvalidRefreshTokenException();
        }

        return toAuthResponse(user);
    }

    @Transactional
    public void logout(LogoutRequest request) {
        String refreshToken = request.refreshToken().trim();
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (!Objects.equals(user.getCurrentRefreshTokenId(), jwtService.extractTokenId(refreshToken))) {
            throw new InvalidRefreshTokenException();
        }
        user.clearCurrentRefreshTokenId();
    }

    private AuthResponse toAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        user.updateCurrentRefreshTokenId(jwtService.extractTokenId(refreshToken));

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                new UserSummary(
                        user.getId(),
                        user.getFullName(),
                        user.getEmail(),
                        user.getRole().name()
                )
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
