package com.smartRestaurant.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.smartRestaurant.security.filter.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize y @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth

                        // ── Actuator ────────────────────────────────────────────────────────
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").access(
                                new org.springframework.security.web.access.expression
                                        .WebExpressionAuthorizationManager(
                                        "hasIpAddress('127.0.0.1') or hasIpAddress('::1') or hasIpAddress('172.18.0.0/16')"
                                )
                        )

                        // ── Auth: endpoints públicos ─────────────────────────────────────────
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/social-login",
                                "/api/auth/verify-2fa",
                                "/api/auth/verify-email",
                                "/api/auth/resend-verification",
                                "/api/auth/resend-2fa",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/api/auth/unlock-account",
                                "/api/auth/refresh-token"
                        ).permitAll()
                        // change-password, /me, /profile, /logout, etc. requieren autenticación
                        .requestMatchers("/api/auth/**").authenticated()

                        // ── Raíz y chatbot (públicos) ────────────────────────────────────────
                        .requestMatchers("/", "/api/chatbot/**").permitAll()

                        // ── Imágenes: GET público; POST/DELETE solo ADMIN o KITCHEN ─────────
                        .requestMatchers(HttpMethod.GET, "/api/images/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/images/**").hasAnyRole("ADMIN", "KITCHEN")
                        .requestMatchers(HttpMethod.DELETE, "/api/images/**").hasAnyRole("ADMIN", "KITCHEN")

                        // ── Restaurante: GET público; PUT solo ADMIN ─────────────────────────
                        .requestMatchers(HttpMethod.GET, "/api/restaurant", "/api/restaurant/is-open").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/restaurant").hasRole("ADMIN")

                        // ── Menú diario: GET público; escritura/borrado solo ADMIN o KITCHEN ─
                        .requestMatchers(HttpMethod.GET, "/api/dailyMenus/**").permitAll()
                        .requestMatchers("/api/dailyMenus/**").hasAnyRole("ADMIN", "KITCHEN")

                        // ── Admin y Dashboard ────────────────────────────────────────────────
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

                        // ── Mesas: GET y PATCH para ADMIN/WAITER; POST/PUT/DELETE solo ADMIN ─
                        .requestMatchers(HttpMethod.GET, "/api/tables/**").hasAnyRole("ADMIN", "WAITER")
                        .requestMatchers(HttpMethod.PATCH, "/api/tables/**").hasAnyRole("ADMIN", "WAITER")
                        .requestMatchers("/api/tables/**").hasRole("ADMIN")

                        // ── Inventario: solo ADMIN y KITCHEN ────────────────────────────────
                        .requestMatchers("/api/categories/**").hasAnyRole("ADMIN", "KITCHEN")
                        .requestMatchers("/api/products/**").hasAnyRole("ADMIN", "KITCHEN")
                        .requestMatchers("/api/supliers/**").hasAnyRole("ADMIN", "KITCHEN")
                        .requestMatchers("/api/inventory/**").hasAnyRole("ADMIN", "KITCHEN")

                        // ── Platos, bebidas, adiciones: GET para todos los roles autenticados;
                        //    escritura solo ADMIN y KITCHEN (@PreAuthorize refina más) ─────────
                        .requestMatchers(HttpMethod.GET, "/api/dishes/**").authenticated()
                        .requestMatchers("/api/dishes/**").hasAnyRole("ADMIN", "KITCHEN")
                        .requestMatchers(HttpMethod.GET, "/api/drinks/**").authenticated()
                        .requestMatchers("/api/drinks/**").hasAnyRole("ADMIN", "KITCHEN")
                        .requestMatchers(HttpMethod.GET, "/api/additions/**").authenticated()
                        .requestMatchers("/api/additions/**").hasAnyRole("ADMIN", "KITCHEN")

                        // ── Órdenes, pagos, facturas y SSE: autenticados
                        //    (la autorización por rol la maneja @PreAuthorize en cada método) ──
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/payments/**").authenticated()
                        .requestMatchers("/api/invoices/**").authenticated()
                        .requestMatchers("/api/sse/**").authenticated()

                        // ── Cualquier otro endpoint requiere autenticación ────────────────────
                        .anyRequest().authenticated()
                )

                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
