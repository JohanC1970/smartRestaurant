package com.smartRestaurant.auth.service.impl;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartRestaurant.auth.dto.SocialUserInfo;
import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.SocialLoginRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;
import com.smartRestaurant.auth.model.entity.RefreshToken;
import com.smartRestaurant.auth.model.entity.SocialAccount;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.model.enums.OtpTokenType;
import com.smartRestaurant.auth.model.enums.SocialProvider;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.RefreshTokenRepository;
import com.smartRestaurant.auth.repository.SocialAccountRepository;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.AuditService;
import com.smartRestaurant.auth.service.AuthenticationService;
import com.smartRestaurant.auth.service.EmailService;
import com.smartRestaurant.auth.service.SocialAuthValidator;
import com.smartRestaurant.common.exception.AccountLockedException;
import com.smartRestaurant.common.exception.EmailAlreadyExistsException;
import com.smartRestaurant.common.exception.InvalidCredentialsException;
import com.smartRestaurant.common.exception.InvalidOtpException;
import com.smartRestaurant.common.exception.UserNotFoundException;
import com.smartRestaurant.security.service.JwtService;
import com.smartRestaurant.auth.service.OtpService;
import com.smartRestaurant.auth.util.PasswordGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

        private final UserRepository userRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final SocialAccountRepository socialAccountRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final OtpService otpService;
        private final EmailService emailService;
        private final AuditService auditService;
        private final SocialAuthValidator socialAuthValidator;

        @Override
        @Transactional
        public void registerPublic(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new EmailAlreadyExistsException(request.getEmail());
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
                        throw new EmailAlreadyExistsException(request.getEmail());
                }

                // RF-02: Generar contraseña temporal aleatoria
                String tempPassword = PasswordGenerator.generate();

                User user = User.builder()
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(tempPassword))
                                .role(request.getRole())
                                .status(UserStatus.ACTIVE) // Usuario activo inmediatamente
                                .isEmailVerified(true) // Email verificado automáticamente
                                .requiresPasswordChange(true) // RF-02: Forzar cambio de contraseña
                                .build();

                userRepository.save(user);

                // RF-02: Enviar email solo con contraseña temporal (sin OTP)
                emailService.sendEmployeeCredentials(user.getEmail(), user.getFirstName(), tempPassword, null);

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
                        throw new InvalidCredentialsException();
                }

                // Verificar si la cuenta está bloqueada
                if (user.isLocked()) {
                        auditService.logEvent(user, AuditEventType.LOGIN_FAILED,
                                        "Intento de login con cuenta bloqueada", null, null, false);
                        throw new AccountLockedException(
                                        "La cuenta está bloqueada. Revise su email para desbloquearla.");
                }

                // Verificar si la cuenta está inactiva
                if (user.getStatus() == UserStatus.INACTIVE) {
                        auditService.logEvent(user, AuditEventType.LOGIN_FAILED,
                                        "Intento de login con cuenta inactiva", null, null, false);
                        throw new com.smartRestaurant.common.exception.AccountInactiveException();
                }

                // Verificar si la cuenta está pendiente de verificación
                if (user.getStatus() == UserStatus.PENDING) {
                        auditService.logEvent(user, AuditEventType.LOGIN_FAILED,
                                        "Intento de login con cuenta pendiente de verificación", null, null, false);
                        throw new com.smartRestaurant.common.exception.AccountPendingException();
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
                                                "Cuenta bloqueada por " + user.getFailedLoginAttempts()
                                                                + " intentos fallidos",
                                                null, null);

                                throw new AccountLockedException(
                                                "Cuenta bloqueada por intentos fallidos. Revise su email para desbloquearla.");
                        }

                        userRepository.save(user);

                        // RF-13: Auditoría de login fallido
                        auditService.logEvent(user, AuditEventType.LOGIN_FAILED,
                                        "Contraseña incorrecta. Intento " + user.getFailedLoginAttempts() + " de 3",
                                        null, null, false);

                        throw new InvalidCredentialsException();
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
                                .requiresPasswordChange(user.isRequiresPasswordChange()) // RF-02: Indicar si requiere
                                                                                         // cambio
                                .build();
        }

        @Override
        @Transactional
        public AuthResponse verify2fa(VerifyRequest request) {
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new UserNotFoundException());

                // Verificar si la cuenta está bloqueada
                if (user.isLocked()) {
                        auditService.logEvent(user, AuditEventType.TWO_FA_FAILED,
                                        "Intento de 2FA con cuenta bloqueada", null, null, false);
                        throw new AccountLockedException(
                                        "La cuenta está bloqueada. Revise su email para desbloquearla.");
                }

                // Verificar si la cuenta está inactiva
                if (user.getStatus() == UserStatus.INACTIVE) {
                        auditService.logEvent(user, AuditEventType.TWO_FA_FAILED,
                                        "Intento de 2FA con cuenta inactiva", null, null, false);
                        throw new com.smartRestaurant.common.exception.AccountInactiveException();
                }

                // Verificar si la cuenta está pendiente de verificación
                if (user.getStatus() == UserStatus.PENDING) {
                        auditService.logEvent(user, AuditEventType.TWO_FA_FAILED,
                                        "Intento de 2FA con cuenta pendiente de verificación", null, null, false);
                        throw new com.smartRestaurant.common.exception.AccountPendingException();
                }

                if (!otpService.validateOtp(user, request.getCode(), OtpTokenType.LOGIN_2FA)) {
                        // RF-13: Auditoría de 2FA fallido
                        auditService.logEvent(user, AuditEventType.TWO_FA_FAILED,
                                        "Código 2FA inválido o expirado", null, null, false);
                        throw new InvalidOtpException();
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
        public AuthResponse changePasswordFirstLogin(String email, String newPassword) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                // Validar que el usuario realmente requiere cambio de contraseña
                if (!user.isRequiresPasswordChange()) {
                        throw new RuntimeException("Este usuario no requiere cambio de contraseña");
                }

                // RF-08: Validar que no reutilice la contraseña anterior
                if (user.getPreviousPasswordHash() != null &&
                                passwordEncoder.matches(newPassword, user.getPreviousPasswordHash())) {
                        throw new RuntimeException("No puede reutilizar su contraseña anterior");
                }

                // Guardar contraseña anterior y actualizar
                user.setPreviousPasswordHash(user.getPassword());
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setRequiresPasswordChange(false); // Ya cambió la contraseña
                userRepository.save(user);

                // Generar nuevos tokens para mantener la sesión activa
                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                // Guardar refresh token en base de datos
                RefreshToken refreshTokenEntity = RefreshToken.builder()
                                .usuario(user)
                                .token(refreshToken)
                                .build();
                refreshTokenRepository.save(refreshTokenEntity);

                // RF-13: Auditoría
                auditService.logEvent(user, AuditEventType.PASSWORD_CHANGED,
                                "Contraseña cambiada en primer login", null, null);

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .message("Contraseña cambiada exitosamente")
                                .is2faRequired(false)
                                .requiresPasswordChange(false)
                                .build();
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

        @Override
        @Transactional
        public void resendVerification(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                if (user.isEmailVerified()) {
                        throw new RuntimeException("El email ya está verificado");
                }

                // Generar nuevo OTP de verificación
                String otp = otpService.generateOtp(user, OtpTokenType.VERIFICACION_EMAIL);
                emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), otp);
        }

        @Override
        @Transactional(readOnly = true)
        public com.smartRestaurant.auth.dto.response.UserResponse getCurrentUser(String email) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

                return com.smartRestaurant.auth.dto.response.UserResponse.builder()
                                .id(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .roleDisplayName(user.getRole().getDisplayName())
                                .status(user.getStatus())
                                .statusDisplayName(user.getStatus().getDisplayName())
                                .isEmailVerified(user.isEmailVerified())
                                .requiresPasswordChange(user.isRequiresPasswordChange())
                                .failedLoginAttempts(user.getFailedLoginAttempts())
                                .lockReason(user.getLockReason())
                                .lockedAt(user.getLockedAt())
                                .createdAt(user.getCreatedAt())
                                .updatedAt(user.getUpdatedAt())
                                .build();
        }

        @Override
        @Transactional
        public AuthResponse socialLogin(SocialLoginRequest request) {
                // 1. Validar token y obtener información del usuario
                SocialUserInfo socialUserInfo = socialAuthValidator.validateAndGetUserInfo(
                                request.getProvider(),
                                request.getAccessToken());

                // 2. Verificar si ya existe una cuenta social vinculada
                java.util.Optional<SocialAccount> existingSocialAccount = socialAccountRepository
                                .findByProviderAndProviderId(request.getProvider(),
                                                socialUserInfo.getProviderId());

                User user;

                if (existingSocialAccount.isPresent()) {
                        // Usuario existente con cuenta social vinculada
                        user = existingSocialAccount.get().getUsuario();

                        // Actualizar información si es necesario
                        updateSocialAccount(existingSocialAccount.get(),
                                        socialUserInfo);

                } else {
                        // Verificar si existe un usuario con el mismo email
                        java.util.Optional<User> existingUser = userRepository.findByEmail(socialUserInfo.getEmail());

                        if (existingUser.isPresent()) {
                                // Vincular cuenta social a usuario existente
                                user = existingUser.get();
                                createSocialAccount(user, request.getProvider(),
                                                socialUserInfo);

                        } else {
                                // Crear nuevo usuario
                                user = createUserFromSocialLogin(socialUserInfo);
                                createSocialAccount(user, request.getProvider(),
                                                socialUserInfo);
                        }
                }

                // 3. Verificar estado del usuario
                if (!user.getStatus().canLogin()) {
                        throw new AccountLockedException("La cuenta está " +
                                        user.getStatus().getDisplayName());
                }

                // 4. Generar tokens
                String accessToken = jwtService.generateAccessToken(user);
                String refreshToken = jwtService.generateRefreshToken(user);

                // 5. Guardar refresh token
                RefreshToken refreshTokenEntity = RefreshToken.builder()
                                .usuario(user)
                                .token(refreshToken)
                                .build();
                refreshTokenRepository.save(refreshTokenEntity);

                // 6. Registrar auditoría
                auditService.logEvent(
                                user,
                                AuditEventType.LOGIN_SUCCESS,
                                "Login exitoso con " + request.getProvider().getDisplayName(),
                                null,
                                null);

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .message("Login exitoso con " +
                                                request.getProvider().getDisplayName())
                                .is2faRequired(false)
                                .requiresPasswordChange(false)
                                .build();
        }

        /**
         * Crea un nuevo usuario desde información de login social
         */
        private User createUserFromSocialLogin(SocialUserInfo socialUserInfo) {
                User user = User.builder()
                                .firstName(socialUserInfo.getFirstName())
                                .lastName(socialUserInfo.getLastName())
                                .email(socialUserInfo.getEmail())
                                .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Password aleatorio
                                .role(UserRole.CUSTOMER) // Por defecto CUSTOMER
                                .status(socialUserInfo.isEmailVerified() ? UserStatus.ACTIVE : UserStatus.PENDING)
                                .isEmailVerified(socialUserInfo.isEmailVerified())
                                .requiresPasswordChange(false)
                                .build();

                user = userRepository.save(user);

                // Registrar auditoría
                auditService.logEvent(user, AuditEventType.USER_REGISTERED,
                                "Usuario registrado vía login social", null, null);

                return user;
        }

        /**
         * Crea una cuenta social vinculada a un usuario
         */
        private void createSocialAccount(User user, SocialProvider provider,
                        SocialUserInfo socialUserInfo) {
                SocialAccount socialAccount = SocialAccount.builder()
                                .usuario(user)
                                .provider(provider)
                                .providerId(socialUserInfo.getProviderId())
                                .profilePictureUrl(socialUserInfo.getProfilePicture())
                                .build();

                socialAccountRepository.save(socialAccount);
        }

        /**
         * Actualiza información de una cuenta social existente
         */
        private void updateSocialAccount(SocialAccount socialAccount, SocialUserInfo socialUserInfo) {
                socialAccount.setProfilePictureUrl(socialUserInfo.getProfilePicture());
                socialAccountRepository.save(socialAccount);
        }
}
