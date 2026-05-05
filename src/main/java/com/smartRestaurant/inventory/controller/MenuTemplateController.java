package com.smartRestaurant.inventory.controller;

import com.smartRestaurant.inventory.Service.MenuTemplateService;
import com.smartRestaurant.inventory.dto.ResponseDTO;
import com.smartRestaurant.inventory.dto.menu.request.CreateMenuTemplateRequest;
import com.smartRestaurant.inventory.dto.menu.response.MenuTemplateDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu/templates")
@RequiredArgsConstructor
public class MenuTemplateController {

    private final MenuTemplateService menuTemplateService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('daily_menu:write', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> create(@RequestBody @Valid CreateMenuTemplateRequest request) {
        menuTemplateService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("Plantilla creada exitosamente", false));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('daily_menu:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<List<MenuTemplateDTO>>> getAll() {
        return ResponseEntity.ok(new ResponseDTO<>(menuTemplateService.getAll(), false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('daily_menu:read', 'ROLE_ADMIN', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<MenuTemplateDTO>> getById(@PathVariable String id) {
        return ResponseEntity.ok(new ResponseDTO<>(menuTemplateService.getById(id), false));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('daily_menu:delete', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> delete(@PathVariable String id) {
        menuTemplateService.delete(id);
        return ResponseEntity.ok(new ResponseDTO<>("Plantilla eliminada", false));
    }
}
