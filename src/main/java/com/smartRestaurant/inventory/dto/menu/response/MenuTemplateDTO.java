package com.smartRestaurant.inventory.dto.menu.response;

import java.util.List;

public record MenuTemplateDTO(
        String id,
        String name,
        String description,
        List<TemplateSectionDTO> sections
) {}
