package com.smartRestaurant.auth.model.entity;

import java.time.LocalDateTime;

import com.smartRestaurant.auth.model.enums.OtpTokenType;

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
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @Column(name = "codigo_otp", nullable = false, length = 6)
    private String codigoOtp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpTokenType tipo;

    @Column(name = "fecha_expiracion", nullable = false)
    private LocalDateTime fechaExpiracion;

    @Column(nullable = false)
    @Builder.Default
    private boolean usado = false;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        fechaCreacion = LocalDateTime.now();
        if (fechaExpiracion == null && tipo != null) {
            // Establece la fecha de expiración basada en el tipo de OTP
            fechaExpiracion = fechaCreacion.plusMinutes(tipo.getExpirationMinutes());
        }
    }

    // Métodos de utilidad

    /**
     * Verifica si el token OTP ha expirado
     * 
     * @return true si el token ha expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(fechaExpiracion);
    }

    /**
     * Verifica si el token OTP es válido (no usado y no expirado)
     * 
     * @return true si el token es válido
     */
    public boolean isValid() {
        return !usado && !isExpired();
    }

    /**
     * Marca el token como usado
     */
    public void markAsUsed() {
        this.usado = true;
    }

    /**
     * Verifica si el código OTP proporcionado coincide con el almacenado
     * 
     * @param codigo Código a verificar
     * @return true si el código coincide
     */
    public boolean matchesCode(String codigo) {
        return this.codigoOtp != null && this.codigoOtp.equals(codigo);
    }

    /**
     * Verifica si el token es válido y coincide con el código proporcionado
     * 
     * @param codigo Código a verificar
     * @return true si el token es válido y el código coincide
     */
    public boolean isValidAndMatches(String codigo) {
        return isValid() && matchesCode(codigo);
    }
}
