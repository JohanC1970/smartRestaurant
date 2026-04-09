package com.smartRestaurant.auth.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Usuario no encontrado con email: " + email));

                // Convertir User a UserDetails de Spring Security con permisos granulares
                return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities(user.getRole().getAuthorities()) // Incluye rol y permisos
                                .accountLocked(user.isLocked())
                                .disabled(user.getStatus() != com.smartRestaurant.auth.model.enums.UserStatus.ACTIVE
                                                && user.getStatus() != com.smartRestaurant.auth.model.enums.UserStatus.PENDING)
                                .build();
        }
}
