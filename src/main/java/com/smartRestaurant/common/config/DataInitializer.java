package com.smartRestaurant.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.init.admin.password:Admin123!}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        try {
            if (!userRepository.existsByEmail(adminEmail)) {
                User admin = User.builder()
                        .firstName("Admin")
                        .lastName("User")
                        .email(adminEmail)
                        .password(passwordEncoder.encode(adminPassword))
                        .role(UserRole.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .isEmailVerified(true)
                        .requiresPasswordChange(false)
                        .build();

                userRepository.save(admin);
                System.out.println("[DataInitializer] Admin creado: " + adminEmail + " / " + adminPassword);
            }
        } catch (Exception e) {
            System.err.println("[DataInitializer] Error creando admin: " + e.getMessage());
        }
    }
}
