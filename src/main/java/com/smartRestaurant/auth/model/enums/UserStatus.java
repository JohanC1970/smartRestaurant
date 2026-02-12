package com.smartRestaurant.auth.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Enum que define los estados posibles de un usuario en el sistema.
 * Cada estado determina qué acciones puede realizar el usuario.
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {

    // Usuario activo - Puede usar todas las funcionalidades del sistema
    ACTIVE("Activo", "Usuario con acceso completo al sistema"),

    // Usuario inactivo - Temporalmente deshabilitado, puede ser reactivado
    INACTIVE("Inactivo", "Usuario temporalmente deshabilitado"),

    // Usuario pendiente - Esperando verificación de email o aprobación
    PENDING("Pendiente", "Usuario pendiente de verificación"),

    // Usuario baneado - Bloqueado permanentemente por violación de políticas
    BANNED("Baneado", "Usuario bloqueado por el administrador");

    // Nombre legible para mostrar en la interfaz de usuario
    private final String displayName;

    // Descripción detallada del estado
    private final String description;

    /**
     * Verifica si el usuario puede iniciar sesión
     * Solo usuarios ACTIVE pueden hacer login
     * 
     * @return true si el usuario puede iniciar sesión
     */
    public boolean canLogin() {
        return this == ACTIVE;
    }

    /**
     * Verifica si el usuario puede recibir emails del sistema
     * Usuarios BANNED no reciben emails
     * 
     * @return true si el usuario puede recibir emails
     */
    public boolean canReceiveEmails() {
        return this != BANNED;
    }

    /**
     * Verifica si el usuario está bloqueado (temporal o permanentemente)
     * 
     * @return true si el usuario está bloqueado
     */
    public boolean isBlocked() {
        return this == INACTIVE || this == BANNED;
    }

    /**
     * Verifica si el usuario necesita verificación
     * 
     * @return true si el usuario está en estado PENDING
     */
    public boolean needsVerification() {
        return this == PENDING;
    }

    /**
     * Verifica si el estado permite reactivación
     * Solo INACTIVE y PENDING pueden ser reactivados a ACTIVE
     * 
     * @return true si el usuario puede ser reactivado
     */
    public boolean canBeReactivated() {
        return this == INACTIVE || this == PENDING;
    }

    /**
     * Verifica si el estado es permanente (no puede cambiar sin intervención admin)
     * 
     * @return true si el estado es BANNED
     */
    public boolean isPermanent() {
        return this == BANNED;
    }
}
