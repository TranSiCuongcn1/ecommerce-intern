package com.trancuong.ecommerce.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.auth.dto.AuthResponse;
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
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_normalizesEmailCreatesCustomerAndStoresCurrentTokenIds() {
        User savedUser = user("test@example.com", Role.CUSTOMER);

        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        stubGeneratedTokens(savedUser);

        AuthResponse response = authService.register(new RegisterRequest(
                " Test User ",
                " TEST@Example.COM ",
                "password123"
        ));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.user().email()).isEqualTo("test@example.com");
        assertThat(savedUser.getCurrentAccessTokenId()).isEqualTo("access-id");
        assertThat(savedUser.getCurrentRefreshTokenId()).isEqualTo("refresh-id");
    }

    @Test
    void register_whenEmailExists_throwsDuplicateEmail() {
        when(userRepository.existsByEmailIgnoreCase("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest(
                "Test User",
                "test@example.com",
                "password123"
        ))).isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void login_whenPasswordInvalid_throwsInvalidCredentials() {
        User user = user("test@example.com", Role.CUSTOMER);

        when(userRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad-password", user.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest(
                "test@example.com",
                "bad-password"
        ))).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_whenTokenMatches_rotatesTokens() {
        User user = user("test@example.com", Role.CUSTOMER);
        user.updateCurrentRefreshTokenId("old-refresh-id");

        when(jwtService.isValid("old-refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("old-refresh-token")).thenReturn(true);
        when(jwtService.extractEmail("old-refresh-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.extractTokenId("old-refresh-token")).thenReturn("old-refresh-id");
        stubGeneratedTokens(user);

        AuthResponse response = authService.refresh(new RefreshTokenRequest(" old-refresh-token "));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(user.getCurrentAccessTokenId()).isEqualTo("access-id");
        assertThat(user.getCurrentRefreshTokenId()).isEqualTo("refresh-id");
    }

    @Test
    void logout_whenRefreshTokenMatches_clearsTokenIds() {
        User user = user("test@example.com", Role.CUSTOMER);
        user.updateCurrentAccessTokenId("access-id");
        user.updateCurrentRefreshTokenId("refresh-id");

        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractEmail("refresh-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.extractTokenId("refresh-token")).thenReturn("refresh-id");

        authService.logout(new LogoutRequest(" refresh-token "));

        assertThat(user.getCurrentAccessTokenId()).isNull();
        assertThat(user.getCurrentRefreshTokenId()).isNull();
    }

    @Test
    void logout_whenRefreshTokenIdDoesNotMatch_throwsInvalidRefreshToken() {
        User user = user("test@example.com", Role.CUSTOMER);
        user.updateCurrentRefreshTokenId("refresh-id");

        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractEmail("refresh-token")).thenReturn(user.getEmail());
        when(userRepository.findByEmailIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.extractTokenId("refresh-token")).thenReturn("other-refresh-id");

        assertThatThrownBy(() -> authService.logout(new LogoutRequest("refresh-token")))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    private void stubGeneratedTokens(User user) {
        when(jwtService.generateAccessToken(user)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(user)).thenReturn("refresh-token");
        when(jwtService.extractTokenId("access-token")).thenReturn("access-id");
        when(jwtService.extractTokenId("refresh-token")).thenReturn("refresh-id");
    }

    private User user(String email, Role role) {
        User user = new User("Test User", email, "encoded-password", role);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        return user;
    }
}
