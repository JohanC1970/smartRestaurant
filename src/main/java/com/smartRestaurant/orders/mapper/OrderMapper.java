package com.smartRestaurant.orders.mapper;

import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;
import com.smartRestaurant.orders.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

/**
 * Mapper para transformar entre entities y DTOs
 * Usa MapStruct para automappings
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    /**
     * Convertir CreateOrderDto a Order entity
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "waiter", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "items", ignore = true)
    Order toEntity(CreateOrderDto createOrderDto);

    /**
     * Actualizar Order entity desde UpdateOrderDTO
     */
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "waiter", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "channel", ignore = true)
    void updateOrder(UpdateOrderDTO updateOrderDTO, @MappingTarget Order order);

}
