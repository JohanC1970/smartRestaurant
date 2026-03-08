package com.smartRestaurant.auth.service.impl;

import com.smartRestaurant.auth.dto.request.ChangeRoleRequest;
import com.smartRestaurant.auth.dto.request.UpdateUserRequest;
import com.smartRestaurant.auth.dto.response.UserResponse;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.AuditEventType;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.auth.service.AdminService;
import com.smartRestaurant.auth.service.AuditService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @Override
    public List<UserResponse> getAllUsers(UserRole role, UserStatus status) {
        List<User> users;

        if (role != null && status != null) {
            users = userRepository.findByRoleAndStatus(role, status);
        } else if (role != null) {
            users = userRepository.findByRole(role);
        } else if (status != null) {
            users = userRepository.findByStatus(status);
        } else {
            users = userRepository.findAll();
        }

        return users.stream().map(this::toResponse).toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findUserOrThrow(id);

        String oldName = user.getFullName();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        userRepository.save(user);

        log.info("Admin actualizó usuario ID {}: '{}' → '{}'", id, oldName, user.getFullName());
        auditService.logEvent(user, AuditEventType.USER_UPDATED,
                "Datos actualizados por administrador: nombre cambió de '" + oldName + "' a '" + user.getFullName() + "'",
                null, null);

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse changeRole(Long id, ChangeRoleRequest request) {
        User user = findUserOrThrow(id);

        UserRole oldRole = user.getRole();
        UserRole newRole = request.getRole();

        if (oldRole == newRole) {
            throw new RuntimeException("El usuario ya tiene el rol " + newRole.getDisplayName());
        }

        user.setRole(newRole);
        userRepository.save(user);

        log.info("Admin cambió rol del usuario ID {}: {} → {}", id, oldRole, newRole);
        auditService.logEvent(user, AuditEventType.USER_UPDATED,
                "Rol cambiado de '" + oldRole.getDisplayName() + "' a '" + newRole.getDisplayName() + "'",
                null, null);

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse deactivateUser(Long id) {
        User user = findUserOrThrow(id);

        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("La cuenta ya está inactiva");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("No se puede desactivar una cuenta baneada");
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);

        log.info("Admin desactivó cuenta del usuario ID {}", id);
        auditService.logEvent(user, AuditEventType.USER_UPDATED,
                "Cuenta desactivada por administrador", null, null);

        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse activateUser(Long id) {
        User user = findUserOrThrow(id);

        if (user.getStatus() == UserStatus.ACTIVE) {
            throw new RuntimeException("La cuenta ya está activa");
        }
        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("No se puede activar una cuenta baneada. Use el endpoint de desbloqueo.");
        }

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        log.info("Admin activó cuenta del usuario ID {}", id);
        auditService.logEvent(user, AuditEventType.USER_UPDATED,
                "Cuenta activada por administrador", null, null);

        return toResponse(user);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
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


}
