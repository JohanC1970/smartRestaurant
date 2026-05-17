package com.smartRestaurant.inventory.dto.menu.response;

import com.smartRestaurant.inventory.model.MenuTimeSlot;

import java.time.LocalDate;
import java.util.List;

/**
 * Vista simplificada del menú activo para el proceso de Pedidos.
 * Solo expone lo necesario para que el mesero/cliente pueda hacer sus selecciones.
 */
public record ActiveMenuDTO(
        String id,
        LocalDate date,
        MenuTimeSlot timeSlot,
        double basePrice,
        int availablePortions,
        List<PublicationSectionDTO> sections
) {}
