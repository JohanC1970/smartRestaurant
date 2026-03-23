package com.smartRestaurant.auth.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(nullable = false)
    @Builder.Default
    private boolean isEmailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(length = 255)
    private String lockReason;

    private LocalDateTime lockedAt;

    /**
     * Hash de la contraseña anterior para prevenir reutilización inmediata (RF-08)
     */
    @Column(length = 255)
    private String previousPasswordHash;

    /**
     * Indica si el usuario debe cambiar su contraseña en el próximo login (RF-02)
     * Se usa para contraseñas temporales asignadas por administradores
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean requiresPasswordChange = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * ID del restaurante al que pertenece el usuario.
     * Para administradores (dueños), es su propio ID o un ID único de restaurante.
     * Para empleados, es el ID del administrador que los creó.
     */
    private Long restaurantId;

    // Relaciones

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OtpToken> otpTokens = new ArrayList<>();

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Métodos de utilidad
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isLocked() {
        return status == UserStatus.BANNED || lockedAt != null;
    }

    public void incrementFailedAttempts() {
        this.failedLoginAttempts++;
    }

    public void resetFailedAttempts() {
        this.failedLoginAttempts = 0;
    }

    public void lock(String reason) {
        this.status = UserStatus.BANNED;
        this.lockReason = reason;
        this.lockedAt = LocalDateTime.now();
    }

    public void unlock() {
        this.status = UserStatus.ACTIVE;
        this.lockReason = null;
        this.lockedAt = null;
        this.failedLoginAttempts = 0;
    }
}
