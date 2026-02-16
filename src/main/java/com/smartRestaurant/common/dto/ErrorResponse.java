package com.smartRestaurant.common.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO estándar para respuestas de error
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * Timestamp del error
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Código HTTP del error
     */
    private int status;

    /**
     * Código de error personalizado
     */
    private String errorCode;

    /**
     * Mensaje de error legible para el usuario
     */
    private String message;

    /**
     * Detalles adicionales del error (opcional)
     */
    private String details;

    /**
     * Path del endpoint que generó el error
     */
    private String path;
}
