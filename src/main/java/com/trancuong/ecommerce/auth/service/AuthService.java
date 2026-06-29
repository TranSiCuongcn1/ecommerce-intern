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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

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

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return toAuthResponse(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken().trim();
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }
        if (tokenBlacklistService.isBlacklisted(REFRESH_TOKEN_TYPE, jwtService.extractTokenId(refreshToken))) {
            throw new InvalidRefreshTokenException();
        }

        String email = jwtService.extractEmail(refreshToken);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(InvalidRefreshTokenException::new);

        blacklistToken(REFRESH_TOKEN_TYPE, refreshToken);
        return toAuthResponse(user);
    }

    public void logout(String authorizationHeader, LogoutRequest request) {
        String refreshToken = request.refreshToken().trim();
        if (!jwtService.isValid(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        blacklistToken(REFRESH_TOKEN_TYPE, refreshToken);

        String accessToken = extractBearerToken(authorizationHeader);
        if (accessToken != null && jwtService.isValid(accessToken) && jwtService.isAccessToken(accessToken)) {
            blacklistToken(ACCESS_TOKEN_TYPE, accessToken);
        }
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
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

    private void blacklistToken(String tokenType, String token) {
        tokenBlacklistService.blacklist(
                tokenType,
                jwtService.extractTokenId(token),
                jwtService.getRemainingTtl(token)
        );
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return null;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isBlank() ? null : token;
    }
}
