package com.smartRestaurant.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartRestaurant.auth.dto.request.ChangePasswordRequest;
import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.ResetPasswordRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;
import com.smartRestaurant.auth.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest request) {
        authenticationService.registerPublic(request);
        return ResponseEntity
                .ok(java.util.Map.of("message", "Usuario registrado exitosamente. Por favor verifique su email."));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<AuthResponse> verify2fa(@RequestBody @Valid VerifyRequest request) {
        return ResponseEntity.ok(authenticationService.verify2fa(request));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid VerifyRequest request) {
        authenticationService.verifyEmail(request);
        return ResponseEntity
                .ok(java.util.Map.of("message", "Email verificado exitosamente. Ya puede iniciar sesión."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody VerifyRequest request) {
        authenticationService.resendVerification(request.getEmail());
        return ResponseEntity.ok(java.util.Map.of("message", "Nuevo código de verificación enviado a su correo."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody VerifyRequest request) {
        authenticationService.forgotPassword(request.getEmail());
        return ResponseEntity
                .ok(java.util.Map.of("message", "Si el email existe, se ha enviado un código de recuperación."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(java.util.Map.of("message", "Contraseña restablecida exitosamente."));
    }

    @PostMapping("/unlock-account")
    public ResponseEntity<String> unlockAccount(@RequestBody @Valid VerifyRequest request) {
        authenticationService.unlockAccount(request);
        return ResponseEntity.ok("Cuenta desbloqueada exitosamente.");
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody AuthResponse request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request.getRefreshToken()));
    }

    /**
     * RF-07: Solicita cambio de contraseña voluntario
     */
    @PostMapping("/request-password-change")
    public ResponseEntity<String> requestPasswordChange(@RequestBody VerifyRequest request) {
        authenticationService.requestPasswordChange(request.getEmail());
        return ResponseEntity.ok("Código OTP enviado a su correo para cambio de contraseña.");
    }

    /**
     * RF-07: Cambia la contraseña del usuario
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        authenticationService.changePassword(
                request.getEmail(),
                request.getCurrentPassword(),
                request.getNewPassword(),
                request.getOtp());
        return ResponseEntity.ok("Contraseña cambiada exitosamente.");
    }

    /**
     * Cambia la contraseña en el primer login (sin OTP ni contraseña actual)
     * Mantiene la sesión activa devolviendo nuevos tokens JWT
     */
    @PostMapping("/change-password-first-login")
    public ResponseEntity<AuthResponse> changePasswordFirstLogin(
            @RequestBody @Valid com.smartRestaurant.auth.dto.request.FirstLoginPasswordChangeRequest request) {

        // Validar que las contraseñas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Las contraseñas no coinciden");
        }

        return ResponseEntity.ok(authenticationService.changePasswordFirstLogin(
                request.getEmail(),
                request.getNewPassword()));
    }

    /**
     * RF-12: Cierra la sesión del usuario
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody AuthResponse request) {
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok("Sesión cerrada exitosamente.");
    }

    /**
     * Obtiene la información del usuario actualmente autenticado
     */
    @GetMapping("/me")
    public ResponseEntity<com.smartRestaurant.auth.dto.response.UserResponse> getCurrentUser(
            org.springframework.security.core.Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        String email = authentication.getName();
        return ResponseEntity.ok(authenticationService.getCurrentUser(email));
    }

    /**
     * Login/Registro con proveedor social (Google, Facebook, GitHub)
     */
    @PostMapping("/social-login")
    public ResponseEntity<AuthResponse> socialLogin(
            @RequestBody @Valid com.smartRestaurant.auth.dto.request.SocialLoginRequest request) {
        return ResponseEntity.ok(authenticationService.socialLogin(request));
    }
}
