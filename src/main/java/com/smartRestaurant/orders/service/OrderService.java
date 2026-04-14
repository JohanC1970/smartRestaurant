package com.smartRestaurant.orders.service;

import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.GetOrderDetailDTO;
import com.smartRestaurant.orders.dto.Order.GetOrdersDTO;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderStatus;

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
     * Obtener órdenes con paginación y filtros opcionales
     * @param page número de página
     * @param status filtrar por estado (null = todos)
     * @param channel filtrar por canal (null = todos)
     */
    List<GetOrdersDTO> getAll(int page, OrderStatus status, OrderChannel channel);
    
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

    /**
     * Obtener las órdenes del cliente autenticado
     * @param page número de página
     */
    List<GetOrdersDTO> getMyOrders(int page);
}
