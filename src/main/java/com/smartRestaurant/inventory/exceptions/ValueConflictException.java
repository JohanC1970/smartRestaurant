package com.smartRestaurant.inventory.exceptions;

public class ValueConflictException extends RuntimeException {
    public ValueConflictException(String message) {
        super(message);
    }
}
