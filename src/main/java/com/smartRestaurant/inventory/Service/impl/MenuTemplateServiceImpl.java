package com.smartRestaurant.inventory.Service.impl;

import com.smartRestaurant.inventory.Repository.MenuTemplateRepository;
import com.smartRestaurant.inventory.Service.MenuTemplateService;
import com.smartRestaurant.inventory.dto.menu.request.CreateMenuTemplateRequest;
import com.smartRestaurant.inventory.dto.menu.response.MenuTemplateDTO;
import com.smartRestaurant.inventory.dto.menu.response.TemplateSectionDTO;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.model.MenuTemplate;
import com.smartRestaurant.inventory.model.TemplateSection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuTemplateServiceImpl implements MenuTemplateService {

    private final MenuTemplateRepository menuTemplateRepository;

    @Override
    @Transactional
    public void create(CreateMenuTemplateRequest request) {
        if (menuTemplateRepository.existsByName(request.name())) {
            throw new ValueConflictException("Ya existe una plantilla con ese nombre");
        }

        MenuTemplate template = new MenuTemplate();
        template.setId(UUID.randomUUID().toString());
        template.setName(request.name());
        template.setDescription(request.description());

        List<TemplateSection> sections = request.sections().stream().map(s -> {
            TemplateSection section = new TemplateSection();
            section.setId(UUID.randomUUID().toString());
            section.setName(s.name());
            section.setDescription(s.description());
            section.setRequired(s.required());
            section.setMaxSelections(s.maxSelections());
            section.setDisplayOrder(s.displayOrder());
            section.setTemplate(template);
            return section;
        }).toList();

        template.setSections(sections);
        menuTemplateRepository.save(template);
    }

    @Override
    public List<MenuTemplateDTO> getAll() {
        return menuTemplateRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public MenuTemplateDTO getById(String id) {
        return menuTemplateRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Plantilla no encontrada"));
    }

    @Override
    @Transactional
    public void delete(String id) {
        if (!menuTemplateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Plantilla no encontrada");
        }
        menuTemplateRepository.deleteById(id);
    }

    private MenuTemplateDTO toDTO(MenuTemplate template) {
        List<TemplateSectionDTO> sections = template.getSections().stream()
                .map(s -> new TemplateSectionDTO(
                        s.getId(), s.getName(), s.getDescription(),
                        s.isRequired(), s.getMaxSelections(), s.getDisplayOrder()))
                .toList();
        return new MenuTemplateDTO(template.getId(), template.getName(), template.getDescription(), sections);
    }
}
