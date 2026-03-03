package com.smartRestaurant.auth.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.smartRestaurant.auth.service.EmailService;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @PostConstruct
    public void init() {

        log.info("EmailService inicializado con remitente: {}", fromEmail);
        testSmtpConnection();
    }

    private void testSmtpConnection() {
        log.info("=== Verificando conexión SMTP con Gmail ===");
        try {
            if (mailSender instanceof JavaMailSenderImpl mailSenderImpl) {
                mailSenderImpl.testConnection();
                log.info("=== Conexión SMTP exitosa ✓ ===");
            } else {
                log.warn("No se pudo verificar la conexión: mailSender no es una instancia de JavaMailSenderImpl");
            }
        } catch (Exception e) {
            log.error("=== FALLO DE CONEXIÓN SMTP ✗ ===");
            log.error("No se pudo conectar al servidor de correo.");
            log.error("Host: smtp.gmail.com | Puerto: 587 | Usuario: {}", fromEmail);
            log.error("Causa: {}", e.getMessage());
            log.error("Stack trace completo:", e);
        }
    }


    @Async
    @Override
    public void sendVerificationEmail(String to, String name, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);

        sendHtmlEmail(to, "Verificación de Cuenta - Smart Restaurant", "email/verification", context);
    }

    @Async
    @Override
    public void sendPasswordRecoveryEmail(String to, String name, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);

        sendHtmlEmail(to, "Recuperación de Contraseña - Smart Restaurant", "email/recovery", context);
    }

    @Async
    @Override
    public void sendAccountUnlockEmail(String to, String name, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);

        sendHtmlEmail(to, "Desbloqueo de Cuenta - Smart Restaurant", "email/unlock", context);
    }

    @Async
    @Override
    public void sendEmployeeCredentials(String to, String name, String tempPassword, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("tempPassword", tempPassword);
        context.setVariable("otp", otp);

        sendHtmlEmail(to, "Bienvenido a Smart Restaurant - Credenciales de Acceso", "email/employee-credentials",
                context);
    }

    @Async
    @Override
    public void sendPasswordChangeEmail(String to, String name, String otp) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);

        sendHtmlEmail(to, "Cambio de Contraseña - Smart Restaurant", "email/password-change", context);
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        log.info("Intentando enviar email a '{}' con plantilla '{}'", to, templateName);
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String htmlContent = templateEngine.process(templateName, context);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("✓ Email enviado exitosamente a '{}' | Asunto: '{}'", to, subject);

<<<<<<< HEAD
        } catch (Exception e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
            // No lanzamos la excepción para no interrumpir el flujo principal
=======
        } catch (MessagingException e) {
            log.error("✗ Error de mensajería al enviar email a '{}'", to);
            log.error("  Mensaje: {}", e.getMessage());
            log.error("  Causa raíz: {}", e.getCause() != null ? e.getCause().getMessage() : "N/A");
            log.error("  Stack trace:", e);
        } catch (MailException e) {
            log.error("✗ Error de Spring Mail al enviar a '{}'", to);
            log.error("  Mensaje: {}", e.getMessage());
            log.error("  Causa raíz: {}", e.getMostSpecificCause().getMessage());
            log.error("  Stack trace:", e);
        } catch (Exception e) {
            log.error("✗ Error inesperado al enviar email a '{}'", to);
            log.error("  Tipo: {}", e.getClass().getName());
            log.error("  Mensaje: {}", e.getMessage());
            log.error("  Stack trace:", e);
>>>>>>> origin/master
        }
    }

}
