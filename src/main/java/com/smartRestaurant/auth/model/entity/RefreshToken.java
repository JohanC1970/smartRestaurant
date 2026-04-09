package com.smartRestaurant.auth.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(nullable = false, unique = true, length = 500)
    private String token;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fechaExpiracion == null) {
            // Por defecto, los refresh tokens expiran en 7 días
            fechaExpiracion = fechaCreacion.plusDays(7);
        }
    }

    // Métodos de utilidad

    /**
     * Verifica si el refresh token ha expirado
     * 
     * @return true si el token ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Verifica si el refresh token es válido (no expirado)
     * 
     * @return true si el token es válido
     */
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * Verifica si el token proporcionado coincide con el almacenado
     * 
     * @param tokenToVerify Token a verificar
     * @return true si el token coincide
     */
    public boolean matchesToken(String tokenToVerify) {
        return this.token != null && this.token.equals(tokenToVerify);
    }

    /**
     * Verifica si el token es válido y coincide con el token proporcionado
     * 
     * @param tokenToVerify Token a verificar
     * @return true si el token es válido y coincide
     */
    public boolean isValidAndMatches(String tokenToVerify) {
        return isValid() && matchesToken(tokenToVerify);
    }

    /**
     * Extiende la fecha de expiración del token
     * 
     * @param days Número de días a extender
     */
    public void extendExpiration(int days) {
        this.fechaExpiracion = LocalDateTime.now().plusDays(days);
    }
}
