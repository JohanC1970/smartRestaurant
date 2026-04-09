package com.smartRestaurant.orders.service;

import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.GetOrderDetailDTO;
import com.smartRestaurant.orders.dto.Order.GetOrdersDTO;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;

import java.util.List;

/**
 * Interfaz del servicio de órdenes
 * Define los contratos para operaciones CRUD de órdenes
 */
public interface OrderService {
    
    /**
     * Crear una nueva orden con sus items
     * @return ID de la orden creada
     */
    String create(CreateOrderDto orderDto);
    
    /**
     * Obtener todas las órdenes con paginación
     * @param page número de página
     */
    List<GetOrdersDTO> getAll(int page);
    
    /**
     * Obtener detalle completo de una orden
     */
    GetOrderDetailDTO getById(String id);
    
    /**
     * Actualizar una orden existente
     */
    void update(String id, UpdateOrderDTO updateOrderDto);
    
    /**
     * Cancelar una orden
     */
    void cancel(String id);
    
    /**
     * Eliminar una orden
     */
    void delete(String id);
}
