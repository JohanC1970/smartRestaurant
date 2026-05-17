package com.smartRestaurant.orders.mapper;

import com.smartRestaurant.inventory.model.SectionOption;
import com.smartRestaurant.orders.dto.menu.GetMenuInstanceDTO;
import com.smartRestaurant.orders.dto.menu.GetMenuSectionExclusionDTO;
import com.smartRestaurant.orders.dto.menu.GetMenuSectionSelectionDTO;
import com.smartRestaurant.orders.model.MenuOrderInstance;
import com.smartRestaurant.orders.model.MenuSectionExclusion;
import com.smartRestaurant.orders.model.MenuSectionSelection;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MenuOrderInstanceMapper {

    @Mapping(target = "publicationId",   source = "publication.id")
    @Mapping(target = "publicationDate", expression = "java(instance.getPublication().getDate().toString())")
    @Mapping(target = "timeSlot",        expression = "java(instance.getPublication().getTimeSlot().name())")
    @Mapping(target = "basePrice",       source = "publication.basePrice")
    GetMenuInstanceDTO toDTO(MenuOrderInstance instance);

    @Mapping(target = "sectionId",     source = "section.id")
    @Mapping(target = "sectionName",   source = "section.name")
    @Mapping(target = "optionId",      source = "selectedOption.id")
    @Mapping(target = "optionName",    expression = "java(resolveOptionName(sel.getSelectedOption()))")
    @Mapping(target = "additionalCost", source = "selectedOption.additionalCost")
    GetMenuSectionSelectionDTO toSelectionDTO(MenuSectionSelection sel);

    @Mapping(target = "sectionId",   source = "section.id")
    @Mapping(target = "sectionName", source = "section.name")
    GetMenuSectionExclusionDTO toExclusionDTO(MenuSectionExclusion excl);

    default String resolveOptionName(SectionOption option) {
        if (option.getDish()     != null) return option.getDish().getName();
        if (option.getDrink()    != null) return option.getDrink().getName();
        if (option.getAddition() != null) return option.getAddition().getName();
        return option.getId();
    }
}
