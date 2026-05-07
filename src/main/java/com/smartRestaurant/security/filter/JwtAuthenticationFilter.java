package com.smartRestaurant.security.filter;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.smartRestaurant.security.service.JwtService;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/register") ||
                path.equals("/api/auth/login") ||
                path.equals("/api/auth/social-login") ||
                path.equals("/api/auth/verify-2fa") ||
                path.equals("/api/auth/verify-email") ||
                path.equals("/api/auth/resend-verification") ||
                path.equals("/api/auth/resend-2fa") ||
                path.equals("/api/auth/forgot-password") ||
                path.equals("/api/auth/reset-password") ||
                path.equals("/api/auth/unlock-account") ||
                path.equals("/api/auth/refresh-token") ||
                path.startsWith("/api/chatbot/") ||
                path.startsWith("/api/images/") ||
                path.equals("/api/restaurant") ||
                path.equals("/api/restaurant/is-open") ||
                path.equals("/actuator/health") ||
                path.equals("/actuator/info");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        // Verificar que el token no esté vacío
        if (jwt.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            userEmail = jwtService.extractUsername(jwt);
        } catch (JwtException e) {
            filterChain.doFilter(request, response);
            return;
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities());
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
