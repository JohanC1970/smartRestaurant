package com.smartRestaurant.auth.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintValidatorContext;

class PasswordValidatorTest {

    private PasswordValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PasswordValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void isValid_ShouldReturnTrue_ForValidPassword() {
        assertTrue(validator.isValid("StrongPass1!", context));
        assertTrue(validator.isValid("Pass@word", context));
        assertTrue(validator.isValid("P@ssWOrd", context));
    }

    @Test
    void isValid_ShouldReturnFalse_ForShortPassword() {
        assertFalse(validator.isValid("Pass1", context)); // 5 chars
    }

    @Test
    void isValid_ShouldReturnFalse_ForMissingUppercase() {
        assertFalse(validator.isValid("weakpass1!", context));
    }

    @Test
    void isValid_ShouldReturnFalse_ForMissingLowercase() {
        assertFalse(validator.isValid("WEAKPASS1!", context));
    }

    @Test
    void isValid_ShouldReturnFalse_ForMissingSpecialChar() {
        assertFalse(validator.isValid("WeakPass12", context));
    }

    @Test
    void isValid_ShouldReturnFalse_ForNull() {
        assertFalse(validator.isValid(null, context));
    }
}
