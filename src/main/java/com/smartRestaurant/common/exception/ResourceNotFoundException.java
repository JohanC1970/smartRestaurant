package com.smartRestaurant.common.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String errorCode;
    
    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "RESOURCE_NOT_FOUND";
    }
    
    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s con ID %d no encontrado", resourceName, id));
        this.errorCode = "RESOURCE_NOT_FOUND";
    }
    
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s '%s' no encontrado", resourceName, identifier));
        this.errorCode = "RESOURCE_NOT_FOUND";
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
