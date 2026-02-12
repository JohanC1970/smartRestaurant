package com.smartRestaurant.auth.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.AuthenticationService;
import com.smartRestaurant.auth.service.EmailService;
import com.smartRestaurant.auth.service.JwtService;
import com.smartRestaurant.auth.service.OtpService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;

    @Override
    @Transactional
    public void registerPublic(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.PENDING)
                .isEmailVerified(false)
                .build();

        userRepository.save(user);

        // Generar y enviar OTP de verificación
        String otp = otpService.generateOtp(user, OtpTokenType.VERIFICACION_EMAIL);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), otp);
    }

    @Override
    @Transactional
    public void registerEmployee(RegisterAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Generar contraseña temporal (podría ser aleatoria, aquí uso "Temp123!"
        // simplificada)
        String tempPassword = "TempPassword123!";

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .role(request.getRole())
                .status(UserStatus.ACTIVE)
                .isEmailVerified(true) // Empleados creados por admin se verifican automáticamente
                .build();

        userRepository.save(user);

        // Enviar credenciales por email (Implementar envío de credenciales en
        // EmailService si es necesario,
        // por ahora reutilizo logica o asumo que admin comunica)
        // TODO: Enviar email con credenciales
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (user.isLocked()) {
            throw new RuntimeException("La cuenta está bloqueada");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();
            if (user.getFailedLoginAttempts() >= 3) {
                user.lock("Demasiados intentos fallidos");
                String otp = otpService.generateOtp(user, OtpTokenType.DESBLOQUEO_CUENTA);
                emailService.sendAccountUnlockEmail(user.getEmail(), user.getFirstName(), otp);
                userRepository.save(user);
                throw new RuntimeException("Cuenta bloqueada por intentos fallidos. Revise su email.");
            }
            userRepository.save(user);
            throw new RuntimeException("Credenciales inválidas");
        }

        // Credenciales válidas
        user.resetFailedAttempts();
        userRepository.save(user);

        // 2FA Obligatorio
        String otp = otpService.generateOtp(user, OtpTokenType.LOGIN_2FA);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), otp); // Reusing template or create
                                                                                       // separate 2FA template

        return AuthResponse.builder()
                .message("Código 2FA enviado a su correo")
                .is2faRequired(true)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verify2fa(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!otpService.validateOtp(user, request.getCode(), OtpTokenType.LOGIN_2FA)) {
            throw new RuntimeException("Código OTP inválido o expirado");
        }

        otpService.consumeOtp(user, request.getCode(), OtpTokenType.LOGIN_2FA);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("Login exitoso")
                .is2faRequired(false)
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado")); // Seguridad: no revelar? user
                                                                                   // requirement says "Validar que el
                                                                                   // email exista"

        String otp = otpService.generateOtp(user, OtpTokenType.RECUPERACION_PASSWORD);
        emailService.sendPasswordRecoveryEmail(user.getEmail(), user.getFirstName(), otp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!otpService.validateOtp(user, otp, OtpTokenType.RECUPERACION_PASSWORD)) {
            throw new RuntimeException("Código OTP inválido");
        }

        otpService.consumeOtp(user, otp, OtpTokenType.RECUPERACION_PASSWORD);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetFailedAttempts();
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("El email ya está verificado");
        }

        if (!otpService.validateOtp(user, request.getCode(), OtpTokenType.VERIFICACION_EMAIL)) {
            throw new RuntimeException("Código OTP inválido o expirado");
        }

        otpService.consumeOtp(user, request.getCode(), OtpTokenType.VERIFICACION_EMAIL);
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockAccount(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!otpService.validateOtp(user, request.getCode(), OtpTokenType.DESBLOQUEO_CUENTA)) {
            throw new RuntimeException("Código inválido");
        }

        otpService.consumeOtp(user, request.getCode(), OtpTokenType.DESBLOQUEO_CUENTA);
        user.unlock();
        userRepository.save(user);
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (jwtService.validateToken(refreshToken)) {
                String accessToken = jwtService.generateAccessToken(user);
                // Optionally rotate refresh token
                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken) // Return same or new
                        .message("Token refrescado exitosamente")
                        .is2faRequired(false)
                        .build();
            }
        }
        throw new RuntimeException("Refresh token inválido o expirado");
    }
}
