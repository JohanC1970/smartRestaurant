package com.smartRestaurant.inventory.Service;

import com.smartRestaurant.inventory.dto.menu.request.*;
import com.smartRestaurant.inventory.dto.menu.response.ActiveMenuDTO;
import com.smartRestaurant.inventory.dto.menu.response.MenuPublicationDTO;
import com.smartRestaurant.inventory.dto.menu.response.MenuPublicationSummaryDTO;
import org.springframework.data.domain.Page;

public interface MenuPublicationService {

    // ── Publicaciones ─────────────────────────────────────────────────────────
    void create(CreateMenuPublicationRequest request);
    Page<MenuPublicationSummaryDTO> getAll(int page);
    MenuPublicationDTO getById(String id);
    void update(String id, UpdateMenuPublicationRequest request);
    void delete(String id);
    void publish(String id);
    void close(String id);
    void adjustPortions(String id, AdjustPortionsRequest request);

    /** Devuelve el menú activo (PUBLISHED) para la franja horaria actual. Null si no hay ninguno. */
    ActiveMenuDTO getActive();

    // ── Secciones ─────────────────────────────────────────────────────────────
    void addSection(String publicationId, AddPublicationSectionRequest request);
    void updateSection(String publicationId, String sectionId, AddPublicationSectionRequest request);
    void deleteSection(String publicationId, String sectionId);

    // ── Opciones ──────────────────────────────────────────────────────────────
    void addOption(String publicationId, String sectionId, AddSectionOptionRequest request);
    void updateOption(String publicationId, String sectionId, String optionId, AddSectionOptionRequest request);
    void deleteOption(String publicationId, String sectionId, String optionId);
    void toggleOption(String publicationId, String sectionId, String optionId);
}
