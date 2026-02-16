package com.smartRestaurant.auth.util;

import java.security.SecureRandom;

/**
 * Utilidad para generar contraseñas temporales seguras.
 * Cumple con las políticas de contraseña del sistema (RF-08).
 */
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "@#$%^&+=!*?.-_";

    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final int DEFAULT_LENGTH = 12;

    /**
     * Genera una contraseña temporal aleatoria de 12 caracteres
     * que cumple con todas las políticas de seguridad.
     * 
     * @return Contraseña temporal segura
     */
    public static String generate() {
        return generate(DEFAULT_LENGTH);
    }

    /**
     * Genera una contraseña temporal aleatoria de la longitud especificada
     * que cumple con todas las políticas de seguridad.
     * 
     * @param length Longitud de la contraseña (mínimo 8)
     * @return Contraseña temporal segura
     * @throws IllegalArgumentException si la longitud es menor a 8
     */
    public static String generate(int length) {
        if (length < 8) {
            throw new IllegalArgumentException("La longitud mínima de la contraseña es 8 caracteres");
        }

        StringBuilder password = new StringBuilder(length);

        // Asegurar al menos un carácter de cada tipo requerido
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(RANDOM.nextInt(SPECIAL_CHARS.length())));

        // Rellenar el resto con caracteres aleatorios
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(RANDOM.nextInt(ALL_CHARS.length())));
        }

        // Mezclar los caracteres para que no sean predecibles
        return shuffleString(password.toString());
    }

    /**
     * Mezcla los caracteres de una cadena de forma aleatoria
     * 
     * @param input Cadena a mezclar
     * @return Cadena mezclada
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();

        for (int i = characters.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char temp = characters[i];
            characters[i] = characters[j];
            characters[j] = temp;
        }

        return new String(characters);
    }
}
