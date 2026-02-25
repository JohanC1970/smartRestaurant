package com.smartRestaurant.auth.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@Service
@Primary
@ConditionalOnProperty(name = "app.use-localstorage", havingValue = "true")
@RequiredArgsConstructor
public class MockUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;

    @Value("${app.mock.admin.email:admin@example.com}")
    private String adminEmail;

    @Value("${app.mock.admin.password:Admin123!}")
    private String adminPassword;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!username.equalsIgnoreCase(adminEmail)) {
            throw new UsernameNotFoundException("Mock admin only. Usuario no encontrado: " + username);
        }

        String encoded = passwordEncoder.encode(adminPassword);

        return org.springframework.security.core.userdetails.User
                .withUsername(adminEmail)
                .password(encoded)
                .roles("ADMIN")
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}
