package com.smartRestaurant.auth.config;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Admin
            createUser("Admin", "Smart", "admin@smart.com", "admin123", UserRole.ADMIN);
            // Kitchen
            createUser("Personal", "Cocina", "cocina@smart.com", "cocina123", UserRole.KITCHEN);
            // Waiter
            createUser("Mesero", "Juan", "mesero@smart.com", "mesero123", UserRole.WAITER);
            // Customer
            createUser("Cliente", "Fiel", "cliente@smart.com", "cliente123", UserRole.CUSTOMER);

            System.out.println("✅ Usuarios base creados correctamente en la base de datos.");
        }
    }

    private void createUser(String firstName, String lastName, String email, String password, UserRole role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
