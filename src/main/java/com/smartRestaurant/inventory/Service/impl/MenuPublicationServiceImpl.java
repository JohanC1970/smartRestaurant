package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.*;
import com.smartRestaurant.inventory.Service.MenuPublicationService;
import com.smartRestaurant.inventory.dto.menu.request.*;
import com.smartRestaurant.inventory.dto.menu.response.*;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuPublicationServiceImpl implements MenuPublicationService {

    private final MenuPublicationRepository publicationRepository;
    private final MenuTemplateRepository templateRepository;
    private final DishRepository dishRepository;
    private final DrinkRepository drinkRepository;
    private final AdditionRepository additionRepository;

    // ── Publicaciones ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void create(CreateMenuPublicationRequest request) {
        if (publicationRepository.existsByDateAndTimeSlotAndStatusNot(
                request.date(), request.timeSlot(), MenuStatus.CLOSED)) {
            throw new ValueConflictException(
                    "Ya existe una publicación activa para esa fecha y franja horaria");
        }

        MenuPublication publication = new MenuPublication();
        publication.setId(UUID.randomUUID().toString());
        publication.setDate(request.date());
        publication.setTimeSlot(request.timeSlot());
        publication.setBasePrice(request.basePrice());
        publication.setTotalPortions(request.totalPortions());
        publication.setAvailablePortions(request.totalPortions());
        publication.setStatus(MenuStatus.DRAFT);
        publication.setCreatedAt(LocalDateTime.now());

        if (request.templateId() != null) {
            MenuTemplate template = templateRepository.findById(request.templateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));
            publication.setTemplate(template);

            List<PublicationSection> sections = template.getSections().stream()
                    .map(ts -> buildSectionFromTemplate(ts, publication))
                    .toList();
            publication.setSections(new ArrayList<>(sections));

        } else if (request.sections() != null && !request.sections().isEmpty()) {
            List<PublicationSection> sections = request.sections().stream()
                    .map(s -> buildSection(s, publication))
                    .toList();
            publication.setSections(new ArrayList<>(sections));
        }

        publicationRepository.save(publication);
    }

    @Override
    public Page<MenuPublicationSummaryDTO> getAll(int page) {
        Pageable pageable = PageRequest.of(page, 10);
        return publicationRepository.findAllByOrderByDateDesc(pageable)
                .map(this::toSummaryDTO);
    }

    @Override
    public MenuPublicationDTO getById(String id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    @Transactional
    public void update(String id, UpdateMenuPublicationRequest request) {
        MenuPublication publication = findOrThrow(id);
        requireStatus(publication, MenuStatus.DRAFT, "Solo se pueden editar publicaciones en borrador");

        int consumed = publication.getTotalPortions() - publication.getAvailablePortions();
        publication.setDate(request.date());
        publication.setTimeSlot(request.timeSlot());
        publication.setBasePrice(request.basePrice());
        publication.setTotalPortions(request.totalPortions());
        publication.setAvailablePortions(Math.max(0, request.totalPortions() - consumed));

        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void delete(String id) {
        MenuPublication publication = findOrThrow(id);
        requireStatus(publication, MenuStatus.DRAFT, "Solo se pueden eliminar publicaciones en borrador");
        publicationRepository.delete(publication);
    }

    @Override
    @Transactional
    public void publish(String id) {
        MenuPublication publication = findOrThrow(id);
        requireStatus(publication, MenuStatus.DRAFT, "Solo se pueden publicar menús en borrador");
        validateForPublishing(publication);
        publication.setStatus(MenuStatus.PUBLISHED);
        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void close(String id) {
        MenuPublication publication = findOrThrow(id);
        if (publication.getStatus() == MenuStatus.DRAFT || publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("El menú no puede cerrarse desde su estado actual");
        }
        publication.setStatus(MenuStatus.CLOSED);
        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void adjustPortions(String id, AdjustPortionsRequest request) {
        MenuPublication publication = findOrThrow(id);
        if (publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("No se puede ajustar un menú cerrado");
        }

        int consumed = publication.getTotalPortions() - publication.getAvailablePortions();
        int newAvailable = Math.max(0, request.totalPortions() - consumed);

        publication.setTotalPortions(request.totalPortions());
        publication.setAvailablePortions(newAvailable);

        if (newAvailable == 0 && publication.getStatus() == MenuStatus.PUBLISHED) {
            publication.setStatus(MenuStatus.SOLD_OUT);
        } else if (newAvailable > 0 && publication.getStatus() == MenuStatus.SOLD_OUT) {
            publication.setStatus(MenuStatus.PUBLISHED);
        }

        publicationRepository.save(publication);
    }

    @Override
    public ActiveMenuDTO getActive() {
        LocalDate today = LocalDate.now();
        MenuTimeSlot currentSlot = getCurrentTimeSlot();

        MenuPublication publication = publicationRepository
                .findByDateAndTimeSlotAndStatus(today, currentSlot, MenuStatus.PUBLISHED)
                .or(() -> publicationRepository.findByDateAndTimeSlotAndStatus(
                        today, MenuTimeSlot.ALL_DAY, MenuStatus.PUBLISHED))
                .orElse(null);

        if (publication == null) return null;

        return new ActiveMenuDTO(
                publication.getId(),
                publication.getDate(),
                publication.getTimeSlot(),
                publication.getBasePrice(),
                publication.getAvailablePortions(),
                buildSectionDTOsForCustomer(publication)
        );
    }

    // ── Secciones ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void addSection(String publicationId, AddPublicationSectionRequest request) {
        MenuPublication publication = findOrThrow(publicationId);
        if (publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("No se pueden agregar secciones a un menú cerrado");
        }
        if (publication.getStatus() == MenuStatus.PUBLISHED || publication.getStatus() == MenuStatus.SOLD_OUT) {
            throw new BadRequestException(
                    "No se puede modificar la estructura de secciones de un menú publicado. Ciérrelo y republique.");
        }

        publication.getSections().add(buildSection(request, publication));
        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void updateSection(String publicationId, String sectionId, AddPublicationSectionRequest request) {
        MenuPublication publication = findOrThrow(publicationId);
        requireStatus(publication, MenuStatus.DRAFT, "Solo se pueden modificar secciones de un menú en borrador");

        PublicationSection section = findSectionOrThrow(publication, sectionId);
        section.setName(request.name());
        section.setDescription(request.description());
        section.setRequired(request.required());
        section.setMaxSelections(request.maxSelections());
        section.setIncludedInBasePrice(request.includedInBasePrice());
        section.setDisplayOrder(request.displayOrder());

        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void deleteSection(String publicationId, String sectionId) {
        MenuPublication publication = findOrThrow(publicationId);
        requireStatus(publication, MenuStatus.DRAFT, "Solo se pueden eliminar secciones de un menú en borrador");
        publication.getSections().removeIf(s -> s.getId().equals(sectionId));
        publicationRepository.save(publication);
    }

    // ── Opciones ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void addOption(String publicationId, String sectionId, AddSectionOptionRequest request) {
        MenuPublication publication = findOrThrow(publicationId);
        if (publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("No se pueden agregar opciones a un menú cerrado");
        }

        PublicationSection section = findSectionOrThrow(publication, sectionId);
        section.getOptions().add(buildOption(request, section));
        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void updateOption(String publicationId, String sectionId, String optionId, AddSectionOptionRequest request) {
        MenuPublication publication = findOrThrow(publicationId);
        if (publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("No se pueden editar opciones de un menú cerrado");
        }

        PublicationSection section = findSectionOrThrow(publication, sectionId);
        SectionOption option = findOptionOrThrow(section, optionId);
        resolveItem(request, option);
        option.setMaxPortions(request.maxPortions());
        option.setAvailablePortions(request.maxPortions());
        option.setAdditionalCost(request.additionalCost());

        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void deleteOption(String publicationId, String sectionId, String optionId) {
        MenuPublication publication = findOrThrow(publicationId);
        if (publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("No se pueden eliminar opciones de un menú cerrado");
        }

        PublicationSection section = findSectionOrThrow(publication, sectionId);
        section.getOptions().removeIf(o -> o.getId().equals(optionId));
        publicationRepository.save(publication);
    }

    @Override
    @Transactional
    public void toggleOption(String publicationId, String sectionId, String optionId) {
        MenuPublication publication = findOrThrow(publicationId);
        if (publication.getStatus() == MenuStatus.CLOSED) {
            throw new BadRequestException("No se pueden modificar opciones de un menú cerrado");
        }

        PublicationSection section = findSectionOrThrow(publication, sectionId);
        SectionOption option = findOptionOrThrow(section, optionId);
        option.setActive(!option.isActive());

        // Si se desactiva la última opción de una sección obligatoria → SOLD_OUT
        if (!option.isActive() && section.isRequired() && publication.getStatus() == MenuStatus.PUBLISHED) {
            boolean noActiveOptions = section.getOptions().stream().noneMatch(SectionOption::isActive);
            if (noActiveOptions) {
                publication.setStatus(MenuStatus.SOLD_OUT);
            }
        }

        // Si se reactiva una opción y el menú estaba SOLD_OUT → intentar reactivar
        if (option.isActive() && publication.getStatus() == MenuStatus.SOLD_OUT) {
            boolean allRequiredHaveOptions = publication.getSections().stream()
                    .filter(PublicationSection::isRequired)
                    .allMatch(s -> s.getOptions().stream().anyMatch(SectionOption::isActive));
            if (allRequiredHaveOptions && publication.getAvailablePortions() > 0) {
                publication.setStatus(MenuStatus.PUBLISHED);
            }
        }

        publicationRepository.save(publication);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private MenuPublication findOrThrow(String id) {
        return publicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Publicación de menú no encontrada"));
    }

    private PublicationSection findSectionOrThrow(MenuPublication publication, String sectionId) {
        return publication.getSections().stream()
                .filter(s -> s.getId().equals(sectionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Sección no encontrada"));
    }

    private SectionOption findOptionOrThrow(PublicationSection section, String optionId) {
        return section.getOptions().stream()
                .filter(o -> o.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Opción no encontrada"));
    }

    private void requireStatus(MenuPublication publication, MenuStatus required, String message) {
        if (publication.getStatus() != required) {
            throw new BadRequestException(message);
        }
    }

    private void validateForPublishing(MenuPublication publication) {
        if (publication.getSections().isEmpty()) {
            throw new BadRequestException("El menú debe tener al menos una sección");
        }

        for (PublicationSection section : publication.getSections()) {
            if (section.getOptions().isEmpty() && section.isRequired()) {
                throw new BadRequestException(
                        "La sección '" + section.getName() + "' es obligatoria y no tiene opciones");
            }

            boolean hasActiveOption = section.getOptions().stream().anyMatch(SectionOption::isActive);
            if (section.isRequired() && !hasActiveOption) {
                throw new BadRequestException(
                        "La sección '" + section.getName() + "' es obligatoria y no tiene opciones activas");
            }

            for (SectionOption option : section.getOptions()) {
                if (option.isActive()) {
                    validateOptionItem(option);
                }
            }
        }
    }

    private void validateOptionItem(SectionOption option) {
        switch (option.getItemType()) {
            case DISH -> {
                Dish dish = option.getDish();
                if (dish == null || dish.getState() == State.INACTIVE) {
                    throw new BadRequestException("Una opción referencia un plato inactivo o inexistente");
                }
                if (dish.getRecipes().isEmpty()) {
                    throw new BadRequestException(
                            "El plato '" + dish.getName() + "' no tiene receta definida");
                }
            }
            case DRINK -> {
                Drink drink = option.getDrink();
                if (drink == null || drink.getState() == State.INACTIVE) {
                    throw new BadRequestException("Una opción referencia una bebida inactiva o inexistente");
                }
            }
            case ADDITION -> {
                Addition addition = option.getAddition();
                if (addition == null || addition.getState() == State.INACTIVE) {
                    throw new BadRequestException("Una opción referencia una adición inactiva o inexistente");
                }
            }
        }
    }

    private SectionOption buildOption(AddSectionOptionRequest request, PublicationSection section) {
        SectionOption option = new SectionOption();
        option.setId(UUID.randomUUID().toString());
        option.setMaxPortions(request.maxPortions());
        option.setAvailablePortions(request.maxPortions());
        option.setAdditionalCost(request.additionalCost());
        option.setActive(true);
        option.setSection(section);
        resolveItem(request, option);
        return option;
    }

    private void resolveItem(AddSectionOptionRequest request, SectionOption option) {
        option.setDish(null);
        option.setDrink(null);
        option.setAddition(null);
        option.setItemType(request.itemType());

        switch (request.itemType()) {
            case DISH -> option.setDish(
                    dishRepository.findById(request.itemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + request.itemId())));
            case DRINK -> option.setDrink(
                    drinkRepository.findById(request.itemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada: " + request.itemId())));
            case ADDITION -> option.setAddition(
                    additionRepository.findById(request.itemId())
                            .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada: " + request.itemId())));
        }
    }

    private PublicationSection buildSection(AddPublicationSectionRequest request, MenuPublication publication) {
        PublicationSection section = new PublicationSection();
        section.setId(UUID.randomUUID().toString());
        section.setName(request.name());
        section.setDescription(request.description());
        section.setRequired(request.required());
        section.setMaxSelections(request.maxSelections());
        section.setIncludedInBasePrice(request.includedInBasePrice());
        section.setDisplayOrder(request.displayOrder());
        section.setPublication(publication);
        return section;
    }

    private PublicationSection buildSectionFromTemplate(TemplateSection ts, MenuPublication publication) {
        PublicationSection section = new PublicationSection();
        section.setId(UUID.randomUUID().toString());
        section.setName(ts.getName());
        section.setDescription(ts.getDescription());
        section.setRequired(ts.isRequired());
        section.setMaxSelections(ts.getMaxSelections());
        section.setIncludedInBasePrice(true);
        section.setDisplayOrder(ts.getDisplayOrder());
        section.setPublication(publication);
        return section;
    }

    private MenuTimeSlot getCurrentTimeSlot() {
        LocalTime now = LocalTime.now();
        if (!now.isBefore(LocalTime.of(11, 0)) && now.isBefore(LocalTime.of(15, 0))) {
            return MenuTimeSlot.LUNCH;
        } else if (!now.isBefore(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(22, 0))) {
            return MenuTimeSlot.DINNER;
        }
        return MenuTimeSlot.ALL_DAY;
    }

    private boolean isOptionAvailable(SectionOption o) {
        return o.isActive()
                && (o.getAvailablePortions() == null || o.getAvailablePortions() > 0);
    }

    /** Vista administrador: muestra TODAS las secciones, incluso las vacías */
    private List<PublicationSectionDTO> buildSectionDTOs(MenuPublication publication) {
        return publication.getSections().stream()
                .map(s -> {
                    List<SectionOptionDTO> options = s.getOptions().stream()
                            .map(this::toOptionDTO)
                            .toList();
                    return new PublicationSectionDTO(
                            s.getId(), s.getName(), s.getDescription(),
                            s.isRequired(), s.getMaxSelections(), s.isIncludedInBasePrice(),
                            s.getDisplayOrder(),
                            options);
                })
                .toList();
    }

    /** Vista cliente: oculta secciones sin opciones disponibles */
    private List<PublicationSectionDTO> buildSectionDTOsForCustomer(MenuPublication publication) {
        return publication.getSections().stream()
                .map(s -> {
                    List<SectionOptionDTO> availableOptions = s.getOptions().stream()
                            .filter(this::isOptionAvailable)
                            .map(this::toOptionDTO)
                            .toList();
                    return new PublicationSectionDTO(
                            s.getId(), s.getName(), s.getDescription(),
                            s.isRequired(), s.getMaxSelections(), s.isIncludedInBasePrice(),
                            s.getDisplayOrder(),
                            availableOptions);
                })
                .filter(s -> !s.options().isEmpty())
                .toList();
    }

    private SectionOptionDTO toOptionDTO(SectionOption option) {
        String itemId = null, itemName = null, itemPhoto = null;
        switch (option.getItemType()) {
            case DISH -> {
                Dish d = option.getDish();
                itemId = d.getId();
                itemName = d.getName();
                itemPhoto = d.getPhotos().isEmpty() ? null : d.getPhotos().get(0);
            }
            case DRINK -> {
                Drink d = option.getDrink();
                itemId = d.getId();
                itemName = d.getName();
                itemPhoto = d.getPhotos().isEmpty() ? null : d.getPhotos().get(0);
            }
            case ADDITION -> {
                Addition a = option.getAddition();
                itemId = a.getId();
                itemName = a.getName();
                itemPhoto = a.getPhotos().isEmpty() ? null : a.getPhotos().get(0);
            }
        }
        return new SectionOptionDTO(
                option.getId(), option.getItemType(), itemId, itemName, itemPhoto,
                option.getAdditionalCost(), option.getMaxPortions(), option.getAvailablePortions(),
                option.isActive()
        );
    }

    private MenuPublicationSummaryDTO toSummaryDTO(MenuPublication p) {
        return new MenuPublicationSummaryDTO(
                p.getId(), p.getDate(), p.getTimeSlot(), p.getBasePrice(),
                p.getTotalPortions(), p.getAvailablePortions(), p.getStatus(),
                p.getTemplate() != null ? p.getTemplate().getName() : null
        );
    }

    private MenuPublicationDTO toDTO(MenuPublication p) {
        return new MenuPublicationDTO(
                p.getId(), p.getDate(), p.getTimeSlot(), p.getBasePrice(),
                p.getTotalPortions(), p.getAvailablePortions(), p.getStatus(),
                p.getTemplate() != null ? p.getTemplate().getId() : null,
                p.getTemplate() != null ? p.getTemplate().getName() : null,
                p.getCreatedAt(),
                buildSectionDTOs(p)
        );
    }
}
