package com.smartRestaurant.auth.model.entity;

import java.time.LocalDateTime;

import com.smartRestaurant.auth.model.enums.SocialProvider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa una cuenta social vinculada a un usuario
 * Permite que los usuarios inicien sesión con Google, Facebook o GitHub
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "social_accounts")
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SocialProvider provider;

    /**
     * ID único del usuario en el proveedor social
     * Por ejemplo: Google user ID, Facebook user ID, GitHub user ID
     */
    @Column(nullable = false, unique = true, length = 255, name = "provider_id")
    private String providerId;

    /**
     * URL de la foto de perfil del usuario en el proveedor social
     */
    @Column(length = 500, name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(nullable = false, name = "linked_at")
    private LocalDateTime linkedAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        linkedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
