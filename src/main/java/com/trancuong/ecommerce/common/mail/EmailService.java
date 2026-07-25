package com.trancuong.ecommerce.common.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@ecommerce.com}")
    private String fromEmail;

    @Value("${app.mail.reset-password-url:http://localhost:8080/api/auth/reset-password}")
    private String resetPasswordUrl;

    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = resetPasswordUrl + "?token=" + token;
        String subject = "Request Password Reset - Ecommerce API";
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; background: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .button { display: inline-block; padding: 12px 24px; color: #ffffff; background-color: #4F46E5; border-radius: 6px; text-decoration: none; font-weight: bold; margin-top: 20px; }
                    .footer { margin-top: 30px; font-size: 12px; color: #6b7280; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Password Reset Request</h2>
                    <p>Hello,</p>
                    <p>We received a request to reset your password. Click the button below or copy the token to reset your password:</p>
                    <p><strong>Token:</strong> %s</p>
                    <a href="%s" class="button" style="color: #ffffff;">Reset Password</a>
                    <p>This token will expire in 15 minutes. If you did not request a password reset, please ignore this email.</p>
                    <div class="footer">
                        <p>&copy; Ecommerce API Service</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(token, resetLink);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    @Async
    public void sendOrderConfirmationEmail(String toEmail, String orderId, String totalAmount) {
        String subject = "Order Confirmation - #" + orderId;
        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; background: #ffffff; padding: 30px; border-radius: 8px; }
                    .highlight { color: #10B981; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Thank you for your order!</h2>
                    <p>Your order <strong>#%s</strong> has been placed successfully.</p>
                    <p>Total Amount: <span class="highlight">$%s</span></p>
                    <p>We are processing your order and will keep you updated.</p>
                </div>
            </body>
            </html>
            """.formatted(orderId, totalAmount);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
        } catch (Exception e) {
            log.error("Unexpected error sending email to {}", toEmail, e);
        }
    }
}
