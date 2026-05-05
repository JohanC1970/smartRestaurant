package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.MenuPublicationService;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.menu.request.*;
import com.smartRestaurant.inventory.dto.menu.response.ActiveMenuDTO;
import com.smartRestaurant.inventory.dto.menu.response.MenuPublicationDTO;
import com.smartRestaurant.inventory.dto.menu.response.MenuPublicationSummaryDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/menu/publications")
@RequiredArgsConstructor
public class MenuPublicationController {

    private final MenuPublicationService menuPublicationService;

    // ── Publicaciones ─────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> create(@RequestBody @Valid CreateMenuPublicationRequest request) {
        menuPublicationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("Publicación de menú creada en borrador", false));
    }

    @GetMapping("/{page}/page")
    @PreAuthorize("hasAnyAuthority('daily_menu:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<Page<MenuPublicationSummaryDTO>>> getAll(@PathVariable int page) {
        return ResponseEntity.ok(new ResponseDTO<>(menuPublicationService.getAll(page), false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('daily_menu:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<MenuPublicationDTO>> getById(@PathVariable String id) {
        return ResponseEntity.ok(new ResponseDTO<>(menuPublicationService.getById(id), false));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> update(
            @PathVariable String id,
            @RequestBody @Valid UpdateMenuPublicationRequest request) {
        menuPublicationService.update(id, request);
        return ResponseEntity.ok(new ResponseDTO<>("Publicación actualizada", false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('daily_menu:delete', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id) {
        menuPublicationService.delete(id);
        return ResponseEntity.ok(new ResponseDTO<>("Publicación eliminada", false));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> publish(@PathVariable String id) {
        menuPublicationService.publish(id);
        return ResponseEntity.ok(new ResponseDTO<>("Menú publicado exitosamente", false));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> close(@PathVariable String id) {
        menuPublicationService.close(id);
        return ResponseEntity.ok(new ResponseDTO<>("Menú cerrado", false));
    }

    @PatchMapping("/{id}/portions")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> adjustPortions(
            @PathVariable String id,
            @RequestBody @Valid AdjustPortionsRequest request) {
        menuPublicationService.adjustPortions(id, request);
        return ResponseEntity.ok(new ResponseDTO<>("Porciones ajustadas", false));
    }

    /** Consulta desde Pedidos: menú activo para la franja horaria actual. */
    @GetMapping("/active")
    public ResponseEntity<ResponseDTO<ActiveMenuDTO>> getActive() {
        ActiveMenuDTO active = menuPublicationService.getActive();
        if (active == null) {
            return ResponseEntity.ok(new ResponseDTO<>(null, false));
        }
        return ResponseEntity.ok(new ResponseDTO<>(active, false));
    }

    // ── Secciones ─────────────────────────────────────────────────────────────

    @PostMapping("/{id}/sections")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> addSection(
            @PathVariable String id,
            @RequestBody @Valid AddPublicationSectionRequest request) {
        menuPublicationService.addSection(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("Sección agregada", false));
    }

    @PutMapping("/{id}/sections/{sectionId}")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> updateSection(
            @PathVariable String id,
            @PathVariable String sectionId,
            @RequestBody @Valid AddPublicationSectionRequest request) {
        menuPublicationService.updateSection(id, sectionId, request);
        return ResponseEntity.ok(new ResponseDTO<>("Sección actualizada", false));
    }

    @DeleteMapping("/{id}/sections/{sectionId}")
    @PreAuthorize("hasAnyAuthority('daily_menu:delete', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteSection(
            @PathVariable String id,
            @PathVariable String sectionId) {
        menuPublicationService.deleteSection(id, sectionId);
        return ResponseEntity.ok(new ResponseDTO<>("Sección eliminada", false));
    }

    // ── Opciones ──────────────────────────────────────────────────────────────

    @PostMapping("/{id}/sections/{sectionId}/options")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> addOption(
            @PathVariable String id,
            @PathVariable String sectionId,
            @RequestBody @Valid AddSectionOptionRequest request) {
        menuPublicationService.addOption(id, sectionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("Opción agregada", false));
    }

    @PutMapping("/{id}/sections/{sectionId}/options/{optionId}")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> updateOption(
            @PathVariable String id,
            @PathVariable String sectionId,
            @PathVariable String optionId,
            @RequestBody @Valid AddSectionOptionRequest request) {
        menuPublicationService.updateOption(id, sectionId, optionId, request);
        return ResponseEntity.ok(new ResponseDTO<>("Opción actualizada", false));
    }

    @DeleteMapping("/{id}/sections/{sectionId}/options/{optionId}")
    @PreAuthorize("hasAnyAuthority('daily_menu:delete', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> deleteOption(
            @PathVariable String id,
            @PathVariable String sectionId,
            @PathVariable String optionId) {
        menuPublicationService.deleteOption(id, sectionId, optionId);
        return ResponseEntity.ok(new ResponseDTO<>("Opción eliminada", false));
    }

    @PatchMapping("/{id}/sections/{sectionId}/options/{optionId}/toggle")
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> toggleOption(
            @PathVariable String id,
            @PathVariable String sectionId,
            @PathVariable String optionId) {
        menuPublicationService.toggleOption(id, sectionId, optionId);
        return ResponseEntity.ok(new ResponseDTO<>("Opción actualizada", false));
    }
}
