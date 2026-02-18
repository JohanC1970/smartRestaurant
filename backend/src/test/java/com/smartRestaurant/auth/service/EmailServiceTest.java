package com.smartRestaurant.auth.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.smartRestaurant.auth.service.impl.EmailServiceImpl;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@smartrestaurant.com");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendVerificationEmail_ShouldProcessTemplateAndSendEmail() {
        // Arrange
        String to = "test@example.com";
        String name = "Test User";
        String otp = "123456";
        String htmlContent = "<html>Content</html>";

        when(templateEngine.process(eq("email/verification"), any(Context.class))).thenReturn(htmlContent);

        // Act
        emailService.sendVerificationEmail(to, name, otp);

        // Assert
        verify(templateEngine).process(eq("email/verification"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordRecoveryEmail_ShouldProcessTemplateAndSendEmail() {
        // Arrange
        String to = "test@example.com";
        String name = "Test User";
        String otp = "123456";
        String htmlContent = "<html>Content</html>";

        when(templateEngine.process(eq("email/recovery"), any(Context.class))).thenReturn(htmlContent);

        // Act
        emailService.sendPasswordRecoveryEmail(to, name, otp);

        // Assert
        verify(templateEngine).process(eq("email/recovery"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendAccountUnlockEmail_ShouldProcessTemplateAndSendEmail() {
        // Arrange
        String to = "test@example.com";
        String name = "Test User";
        String otp = "123456";
        String htmlContent = "<html>Content</html>";

        when(templateEngine.process(eq("email/unlock"), any(Context.class))).thenReturn(htmlContent);

        // Act
        emailService.sendAccountUnlockEmail(to, name, otp);

        // Assert
        verify(templateEngine).process(eq("email/unlock"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }
}
