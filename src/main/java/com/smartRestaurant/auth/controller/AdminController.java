package com.smartRestaurant.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register-employee")
    // @PreAuthorize("hasRole('ADMIN')") - Will configure in SecurityConfig or
    // enable method security
    public ResponseEntity<String> registerEmployee(@RequestBody @Valid RegisterAdminRequest request) {
        authenticationService.registerEmployee(request);
        return ResponseEntity.ok("Empleado registrado exitosamente.");
    }
}
