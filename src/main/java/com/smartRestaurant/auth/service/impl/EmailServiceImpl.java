package com.smartRestaurant.auth.service.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.smartRestaurant.auth.service.EmailService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final TemplateEngine templateEngine;

    @Value("${spring.sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${spring.sendgrid.from-email:smartrestaurant15@gmail.com}")
    private String fromEmail;

    @PostConstruct
    public void init() {
        log.info("EmailService (SendGrid) inicializado con remitente: {}", fromEmail);
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
        log.info("Intentando enviar email a '{}' con plantilla '{}' (vía SendGrid API)", to, templateName);
        try {
            String htmlContent = templateEngine.process(templateName, context);

            Email from = new Email(fromEmail);
            Email recipient = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, recipient, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("✓ Email enviado exitosamente a '{}' | Asunto: '{}' | Status: {}", to, subject,
                        response.getStatusCode());
            } else {
                log.error("✗ Fallo al enviar email a '{}' | Status: {} | Body: {}", to, response.getStatusCode(),
                        response.getBody());
                log.error("  Headers: {}", response.getHeaders());
            }

        } catch (IOException e) {
            log.error("✗ Error de E/S al enviar email a '{}' vía SendGrid", to);
            log.error("  Mensaje: {}", e.getMessage());
        } catch (Exception e) {
            log.error("✗ Error inesperado al enviar email a '{}' vía SendGrid", to);
            log.error("  Mensaje: {}", e.getMessage());
        }
    }

}
