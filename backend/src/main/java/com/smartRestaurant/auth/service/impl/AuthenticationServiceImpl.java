package com.smartRestaurant.auth.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;
import com.smartRestaurant.auth.model.entity.RefreshToken;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.RefreshTokenRepository;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.AuditService;
import com.smartRestaurant.auth.service.AuthenticationService;
import com.smartRestaurant.auth.service.EmailService;
import com.smartRestaurant.security.service.JwtService;
import com.smartRestaurant.auth.service.OtpService;
import com.smartRestaurant.auth.util.PasswordGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final EmailService emailService;
    private final AuditService auditService;

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

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.USER_REGISTERED,
                "Usuario registrado públicamente con rol CUSTOMER", null, null);
    }

    @Override
    @Transactional
    public void registerEmployee(RegisterAdminRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // RF-02: Generar contraseña temporal aleatoria
        String tempPassword = PasswordGenerator.generate();

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(tempPassword))
                .role(request.getRole())
                .status(UserStatus.PENDING) // RF-02: Pendiente hasta verificar OTP
                .isEmailVerified(false)
                .requiresPasswordChange(true) // RF-02: Forzar cambio de contraseña
                .build();

        userRepository.save(user);

        // RF-02: Generar y enviar OTP de verificación
        String otp = otpService.generateOtp(user, OtpTokenType.VERIFICACION_EMAIL);

        // RF-02: Enviar email con credenciales temporales y OTP
        emailService.sendEmployeeCredentials(user.getEmail(), user.getFirstName(), tempPassword, otp);

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.EMPLOYEE_REGISTERED,
                "Empleado registrado por administrador con rol " + request.getRole(), null, null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Buscar usuario
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        // RF-13: Auditoría de intento de login fallido (usuario no existe)
        if (user == null) {
            auditService.logEventByEmail(request.getEmail(), AuditEventType.LOGIN_FAILED,
                    "Usuario no encontrado", null, null, false);
            throw new RuntimeException("Credenciales inválidas");
        }

        // Verificar si la cuenta está bloqueada
        if (user.isLocked()) {
            auditService.logEvent(user, AuditEventType.LOGIN_FAILED,
                    "Intento de login con cuenta bloqueada", null, null, false);
            throw new RuntimeException("La cuenta está bloqueada");
        }

        // Validar contraseña
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.incrementFailedAttempts();

            // RF-10: Bloquear cuenta después de 3 intentos fallidos
            if (user.getFailedLoginAttempts() >= 3) {
                user.lock("Demasiados intentos fallidos");
                String otp = otpService.generateOtp(user, OtpTokenType.DESBLOQUEO_CUENTA);
                emailService.sendAccountUnlockEmail(user.getEmail(), user.getFirstName(), otp);
                userRepository.save(user);

                // RF-13: Auditoría de bloqueo de cuenta
                auditService.logEvent(user, AuditEventType.ACCOUNT_LOCKED,
                        "Cuenta bloqueada por " + user.getFailedLoginAttempts() + " intentos fallidos", null, null);

                throw new RuntimeException("Cuenta bloqueada por intentos fallidos. Revise su email.");
            }

            userRepository.save(user);

            // RF-13: Auditoría de login fallido
            auditService.logEvent(user, AuditEventType.LOGIN_FAILED,
                    "Contraseña incorrecta. Intento " + user.getFailedLoginAttempts() + " de 3", null, null, false);

            throw new RuntimeException("Credenciales inválidas");
        }

        // Credenciales válidas - resetear intentos fallidos
        user.resetFailedAttempts();
        userRepository.save(user);

        // RF-05: 2FA Obligatorio
        String otp = otpService.generateOtp(user, OtpTokenType.LOGIN_2FA);
        emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), otp);

        return AuthResponse.builder()
                .message("Código 2FA enviado a su correo")
                .is2faRequired(true)
                .requiresPasswordChange(user.isRequiresPasswordChange()) // RF-02: Indicar si requiere cambio
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verify2fa(VerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!otpService.validateOtp(user, request.getCode(), OtpTokenType.LOGIN_2FA)) {
            // RF-13: Auditoría de 2FA fallido
            auditService.logEvent(user, AuditEventType.TWO_FA_FAILED,
                    "Código 2FA inválido o expirado", null, null, false);
            throw new RuntimeException("Código OTP inválido o expirado");
        }

        otpService.consumeOtp(user, request.getCode(), OtpTokenType.LOGIN_2FA);

        // RF-13: Auditoría de 2FA exitoso
        auditService.logEvent(user, AuditEventType.TWO_FA_SUCCESS,
                "Verificación 2FA completada exitosamente", null, null);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // Guardar refresh token en base de datos
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .usuario(user)
                .token(refreshToken)
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        // RF-13: Auditoría de login exitoso
        auditService.logEvent(user, AuditEventType.LOGIN_SUCCESS,
                "Login completado exitosamente", null, null);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .message("Login exitoso")
                .is2faRequired(false)
                .requiresPasswordChange(user.isRequiresPasswordChange()) // RF-02
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        String otp = otpService.generateOtp(user, OtpTokenType.RECUPERACION_PASSWORD);
        emailService.sendPasswordRecoveryEmail(user.getEmail(), user.getFirstName(), otp);

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.PASSWORD_RESET_REQUESTED,
                "Solicitud de recuperación de contraseña", null, null);
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

        // RF-08: Guardar contraseña anterior antes de cambiar
        user.setPreviousPasswordHash(user.getPassword());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetFailedAttempts();
        user.setRequiresPasswordChange(false); // Ya no requiere cambio
        userRepository.save(user);

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.PASSWORD_RESET_COMPLETED,
                "Contraseña restablecida mediante recuperación", null, null);
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

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.EMAIL_VERIFIED,
                "Email verificado exitosamente", null, null);
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

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.ACCOUNT_UNLOCKED,
                "Cuenta desbloqueada exitosamente", null, null);
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            if (jwtService.validateToken(refreshToken)) {
                String accessToken = jwtService.generateAccessToken(user);

                // RF-13: Auditoría
                auditService.logEvent(user, AuditEventType.TOKEN_REFRESHED,
                        "Token de acceso renovado", null, null);

                return AuthResponse.builder()
                        .accessToken(accessToken)
                        .refreshToken(refreshToken)
                        .message("Token refrescado exitosamente")
                        .is2faRequired(false)
                        .build();
            }
        }
        throw new RuntimeException("Refresh token inválido o expirado");
    }

    @Override
    @Transactional
    public void requestPasswordChange(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Generar OTP para cambio de contraseña
        String otp = otpService.generateOtp(user, OtpTokenType.RECUPERACION_PASSWORD);
        emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName(), otp);

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.PASSWORD_RESET_REQUESTED,
                "Solicitud de cambio de contraseña voluntario", null, null);
    }

    @Override
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // RF-07: Validar contraseña actual
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }

        // Validar OTP
        if (!otpService.validateOtp(user, otp, OtpTokenType.RECUPERACION_PASSWORD)) {
            throw new RuntimeException("Código OTP inválido o expirado");
        }

        // RF-08: Validar que no reutilice la contraseña anterior
        if (user.getPreviousPasswordHash() != null &&
                passwordEncoder.matches(newPassword, user.getPreviousPasswordHash())) {
            throw new RuntimeException("No puede reutilizar su contraseña anterior");
        }

        otpService.consumeOtp(user, otp, OtpTokenType.RECUPERACION_PASSWORD);

        // RF-08: Guardar contraseña anterior y actualizar
        user.setPreviousPasswordHash(user.getPassword());
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setRequiresPasswordChange(false); // Ya cambió la contraseña
        userRepository.save(user);

        // RF-13: Auditoría
        auditService.logEvent(user, AuditEventType.PASSWORD_CHANGED,
                "Contraseña cambiada voluntariamente", null, null);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // RF-12: Invalidar refresh token
        refreshTokenRepository.deleteByToken(refreshToken);

        // RF-13: Auditoría (sin usuario porque no tenemos contexto de seguridad aquí)
        String userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                auditService.logEvent(user, AuditEventType.LOGOUT,
                        "Usuario cerró sesión", null, null);
            }
        }
    }
}
