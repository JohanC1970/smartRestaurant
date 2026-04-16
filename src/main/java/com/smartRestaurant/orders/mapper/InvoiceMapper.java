package com.smartRestaurant.orders.mapper;

import com.smartRestaurant.orders.dto.invoice.CreateInvoiceDTO;
import com.smartRestaurant.orders.dto.invoice.GetInvoiceDTO;
import com.smartRestaurant.orders.model.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Mapper para transformar entre Invoice entities y DTOs
 * Usa MapStruct para automappings
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface InvoiceMapper {
    
    /**
     * Convertir CreateInvoiceDTO a Invoice entity
     * MapStruct genera automáticamente:
     *  - id: ID único de factura
     *  - status: PENDING
     *  - createdAt: ahora
     *  - payment: null (se asigna después cuando se pague)
     */
    @Mapping(target = "id", expression = "java(generateInvoiceId())")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "order", ignore = true)     // Se asigna en servicio
    @Mapping(target = "payment", ignore = true)   // Se asigna cuando se pague
    @Mapping(target = "version", ignore = true)
    Invoice toEntity(CreateInvoiceDTO dto);
    
    /**
     * Convertir Invoice entity a GetInvoiceDTO
     */
    @Mapping(source = "order.id", target = "orderId")
    GetInvoiceDTO toDTO(Invoice invoice);
    
    /**
     * Método default para generar ID de factura
     * Formato: INV-YYYYMMDD-XXXXX
     */
    default String generateInvoiceId() {
        LocalDateTime now = LocalDateTime.now();
        String datePrefix = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        return "INV-" + datePrefix + "-" + randomPart;
    }
}

