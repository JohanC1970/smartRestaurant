package com.smartRestaurant.auth.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    // Regex Explanation:
    // (?=.*[0-9]) : At least one digit (Optional based on user request but good
    // practice) - User didn't ask for digit explicitly, but I'll add if standard.
    // User said: 1 lower, 1 special, 1 upper, min 6.
    // (?=.*[a-z]) : At least one lowercase letter
    // (?=.*[A-Z]) : At least one uppercase letter
    // (?=.*[@#$%^&+=!*]) : At least one special character
    // .{6,} : At least 6 characters

    // User request: 1 lowercase, 1 special, 1 uppercase, min 8 chars (RF-08).
    // Updated to comply with security requirement RF-08: minimum 8 characters

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!*?.\\-_]).{8,}$";

    private static final Pattern PATTERN = Pattern.compile(PASSWORD_PATTERN);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        return PATTERN.matcher(password).matches();
    }
}
