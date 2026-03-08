package com.smartRestaurant.auth.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartRestaurant.auth.dto.request.ChangeRoleRequest;
import com.smartRestaurant.auth.dto.request.RegisterAdminRequest;
import com.smartRestaurant.auth.dto.request.UpdateUserRequest;
import com.smartRestaurant.auth.dto.response.UserResponse;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import com.smartRestaurant.auth.service.AdminService;
import com.smartRestaurant.auth.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('user:read', 'ROLE_ADMIN')")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    // ── Registro ─────────────────────────────────────────────────────────────

    @PostMapping("/register-employee")
    @PreAuthorize("hasAnyAuthority('user:write', 'ROLE_ADMIN')")
    public ResponseEntity<String> registerEmployee(@RequestBody @Valid RegisterAdminRequest request) {
        authenticationService.registerEmployee(request);
        return ResponseEntity.ok("Empleado registrado exitosamente.");
    }

    // ── Consulta de usuarios ──────────────────────────────────────────────────

    /**
     * Lista todos los usuarios. Se puede filtrar opcionalmente por rol y/o estado.
     *
     * GET /admin/users
     * GET /admin/users?role=WAITER
     * GET /admin/users?status=ACTIVE
     * GET /admin/users?role=KITCHEN&status=INACTIVE
     */
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('user:read', 'ROLE_ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status) {
        return ResponseEntity.ok(adminService.getAllUsers(role, status));
    }

    /**
     * Obtiene el detalle de un usuario por su ID.
     *
     * GET /admin/users/{id}
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('user:read', 'ROLE_ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    // ── Edición ──────────────────────────────────────────────────────────────

    /**
     * Edita nombre y apellido de un usuario.
     *
     * PUT /admin/users/{id}
     * Body: { "firstName": "...", "lastName": "..." }
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('user:write', 'ROLE_ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(adminService.updateUser(id, request));
    }

    /**
     * Cambia el rol de un usuario.
     *
     * PATCH /admin/users/{id}/role
     * Body: { "role": "WAITER" }  →  ADMIN | KITCHEN | WAITER | CUSTOMER
     */
    @PatchMapping("/users/{id}/role")
    @PreAuthorize("hasAnyAuthority('user:write', 'ROLE_ADMIN')")
    public ResponseEntity<UserResponse> changeRole(
            @PathVariable Long id,
            @RequestBody @Valid ChangeRoleRequest request) {
        return ResponseEntity.ok(adminService.changeRole(id, request));
    }

    // ── Activación / Desactivación ────────────────────────────────────────────

    /**
     * Desactiva una cuenta (ACTIVE → INACTIVE).
     *
     * PATCH /admin/users/{id}/deactivate
     */
    @PatchMapping("/users/{id}/deactivate")
    @PreAuthorize("hasAnyAuthority('user:write', 'ROLE_ADMIN')")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.deactivateUser(id));
    }

    /**
     * Reactiva una cuenta (INACTIVE o PENDING → ACTIVE).
     *
     * PATCH /admin/users/{id}/activate
     */
    @PatchMapping("/users/{id}/activate")
    @PreAuthorize("hasAnyAuthority('user:write', 'ROLE_ADMIN')")
    public ResponseEntity<UserResponse> activateUser(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.activateUser(id));
    }
}