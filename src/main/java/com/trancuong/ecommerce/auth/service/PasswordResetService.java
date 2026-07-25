package com.trancuong.ecommerce.auth.service;

import com.trancuong.ecommerce.auth.domain.PasswordResetToken;
import com.trancuong.ecommerce.auth.dto.ForgotPasswordRequest;
import com.trancuong.ecommerce.auth.dto.ResetPasswordRequest;
import com.trancuong.ecommerce.auth.exception.InvalidResetTokenException;
import com.trancuong.ecommerce.auth.repository.PasswordResetTokenRepository;
import com.trancuong.ecommerce.common.mail.EmailService;
import com.trancuong.ecommerce.user.domain.User;
import com.trancuong.ecommerce.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_EXPIRATION_MINUTES = 15;

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().trim().toLowerCase();
        userRepository.findByEmailIgnoreCase(email).ifPresentOrElse(
                user -> {
                    String token = UUID.randomUUID().toString();
                    LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(TOKEN_EXPIRATION_MINUTES);
                    PasswordResetToken resetToken = new PasswordResetToken(user, token, expiryDate);
                    tokenRepository.save(resetToken);

                    emailService.sendPasswordResetEmail(user.getEmail(), token);
                    log.info("Password reset token generated and sent to email {}", email);
                },
                () -> log.info("Password reset requested for non-existent email {}", email)
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.token().trim())
                .orElseThrow(() -> new InvalidResetTokenException("Invalid or expired password reset token"));

        if (resetToken.isUsed()) {
            throw new InvalidResetTokenException("This password reset token has already been used");
        }

        if (resetToken.isExpired()) {
            throw new InvalidResetTokenException("This password reset token has expired");
        }

        User user = resetToken.getUser();
        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
        user.clearCurrentAccessTokenId();
        user.clearCurrentRefreshTokenId();

        resetToken.markUsed();
        tokenRepository.save(resetToken);
        log.info("Password successfully reset for user {}", user.getEmail());
    }
}
