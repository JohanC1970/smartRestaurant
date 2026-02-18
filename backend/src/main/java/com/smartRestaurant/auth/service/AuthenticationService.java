package com.smartRestaurant.auth.service;

import com.smartRestaurant.auth.dto.request.LoginRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.RegisterRequest;
import com.smartRestaurant.auth.dto.request.VerifyRequest;
import com.smartRestaurant.auth.dto.response.AuthResponse;

public interface AuthenticationService {

    void registerPublic(RegisterRequest request);

    void registerEmployee(RegisterAdminRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse verify2fa(VerifyRequest request);

    void verifyEmail(VerifyRequest request);

    void forgotPassword(String email);

    void resetPassword(String email, String otp, String newPassword);

    void unlockAccount(VerifyRequest request);

    AuthResponse refreshToken(String refreshToken);
}
