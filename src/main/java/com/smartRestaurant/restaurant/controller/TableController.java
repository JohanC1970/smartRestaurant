package com.smartRestaurant.restaurant.controller;

import com.smartRestaurant.restaurant.dto.request.ChangeTableStatusDTO;
import com.smartRestaurant.restaurant.dto.request.CreateTableDTO;
import com.smartRestaurant.restaurant.dto.request.UpdateTableDTO;
import com.smartRestaurant.restaurant.dto.response.GetTableDTO;
import com.smartRestaurant.restaurant.model.enums.TableStatus;
import com.smartRestaurant.restaurant.service.TableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    /**
     * GET /api/tables?status=FREE
     * Lista todas las mesas activas, con filtro opcional por estado.
     * Roles: WAITER, ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<List<GetTableDTO>> getAll(
            @RequestParam(required = false) TableStatus status) {
        return ResponseEntity.ok(tableService.getAll(status));
    }

    /**
     * GET /api/tables/{id}
     * Obtiene el detalle de una mesa.
     * Roles: WAITER, ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAITER', 'ADMIN')")
    public ResponseEntity<GetTableDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(tableService.getById(id));
    }

    /**
     * POST /api/tables
     * Crea una nueva mesa.
     * Roles: ADMIN
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GetTableDTO> create(@RequestBody @Valid CreateTableDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tableService.create(dto));
    }

    /**
     * PUT /api/tables/{id}
     * Actualiza capacidad y/o ubicación de una mesa.
     * Roles: ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<GetTableDTO> update(
            @PathVariable String id,
            @RequestBody @Valid UpdateTableDTO dto) {
        return ResponseEntity.ok(tableService.update(id, dto));
    }

    /**
     * PATCH /api/tables/{id}/status
     * Cambia manualmente el estado de una mesa (FREE, OCCUPIED, RESERVED).
     * Roles: WAITER, ADMIN
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ROLE_WAITER', 'ROLE_ADMIN')")
    public ResponseEntity<GetTableDTO> changeStatus(
            @PathVariable String id,
            @RequestBody @Valid ChangeTableStatusDTO dto) {
        return ResponseEntity.ok(tableService.changeStatus(id, dto));
    }

    /**
     * DELETE /api/tables/{id}
     * Desactiva una mesa (no la elimina físicamente).
     * Roles: ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        tableService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
