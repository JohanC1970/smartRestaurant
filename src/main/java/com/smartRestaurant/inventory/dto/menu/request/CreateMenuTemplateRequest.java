package com.smartRestaurant.inventory.dto.menu.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.Length;

import java.util.List;

public record CreateMenuTemplateRequest(
        @NotBlank @Length(max = 100) String name,
        @Length(max = 500) String description,
        @NotEmpty @Valid List<CreateTemplateSectionRequest> sections
) {}
