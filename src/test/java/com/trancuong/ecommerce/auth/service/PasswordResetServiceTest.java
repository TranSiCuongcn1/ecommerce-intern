package com.trancuong.ecommerce.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.trancuong.ecommerce.auth.domain.PasswordResetToken;
import com.trancuong.ecommerce.auth.dto.ForgotPasswordRequest;
import com.trancuong.ecommerce.auth.dto.ResetPasswordRequest;
import com.trancuong.ecommerce.auth.exception.InvalidResetTokenException;
import com.trancuong.ecommerce.auth.repository.PasswordResetTokenRepository;
import com.trancuong.ecommerce.common.mail.EmailService;
import com.trancuong.ecommerce.user.domain.Role;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.repository.UserRepository;
import java.time.LocalDateTime;
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
class PasswordResetServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    void forgotPassword_whenUserExists_savesTokenAndSendsEmail() {
        User user = new User("John Doe", "john@example.com", "hash", Role.CUSTOMER);
        when(userRepository.findByEmailIgnoreCase("john@example.com")).thenReturn(Optional.of(user));

        passwordResetService.forgotPassword(new ForgotPasswordRequest("john@example.com"));

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendPasswordResetEmail(eq("john@example.com"), any(String.class));
    }

    @Test
    void forgotPassword_whenUserDoesNotExist_doesNotSaveToken() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        passwordResetService.forgotPassword(new ForgotPasswordRequest("unknown@example.com"));

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(any(), any());
    }

    @Test
    void resetPassword_whenTokenValid_updatesUserPassword() {
        User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(user, tokenStr, LocalDateTime.now().plusMinutes(15));

        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        passwordResetService.resetPassword(new ResetPasswordRequest(tokenStr, "new-password"));

        assertThat(user.getPasswordHash()).isEqualTo("new-hash");
        assertThat(token.isUsed()).isTrue();
        verify(tokenRepository).save(token);
    }

    @Test
    void resetPassword_whenTokenExpired_throwsException() {
        User user = new User("John Doe", "john@example.com", "old-hash", Role.CUSTOMER);
        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken(user, tokenStr, LocalDateTime.now().minusMinutes(5));

        when(tokenRepository.findByToken(tokenStr)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> passwordResetService.resetPassword(new ResetPasswordRequest(tokenStr, "new-password")))
                .isInstanceOf(InvalidResetTokenException.class)
                .hasMessageContaining("expired");
    }
}
