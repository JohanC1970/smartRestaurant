package com.smartRestaurant.orders.controller;

import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.GetOrderDetailDTO;
import com.smartRestaurant.orders.dto.Order.GetOrdersDTO;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;
import com.smartRestaurant.orders.dto.ResponseDTO;
import com.smartRestaurant.orders.dto.invoice.GetInvoiceDTO;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.orders.service.InvoiceService;
import com.smartRestaurant.orders.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar órdenes
 * Endpoints para CRUD de órdenes
 */
@RestController
@RequestMapping("api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final InvoiceService invoiceService;

    /**
     * POST /api/orders
     * Crear una nueva orden con sus items
     * Roles permitidos: CUSTOMER, WAITER, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<String>> createOrder(@RequestBody @Valid CreateOrderDto createOrderDto) {

        String orderId = orderService.create(createOrderDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>(orderId, false));
    }

    /**
     * GET /api/orders/{page}/page?status=PENDING&channel=ONLINE
     * Obtener órdenes con paginación y filtros opcionales
     * Roles permitidos: WAITER, KITCHEN, ADMIN
     */
    @GetMapping("/{page}/page")
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<List<GetOrdersDTO>>> getAllOrders(
            @PathVariable int page,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) OrderChannel channel) {

        List<GetOrdersDTO> orders = orderService.getAll(page, status, channel);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(orders, false));
    }

    /**
     * GET /api/orders/{id}
     * Obtener detalle completo de una orden
     * Roles permitidos: WAITER, KITCHEN, ADMIN
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_KITCHEN', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<GetOrderDetailDTO>> getOrderById(@PathVariable String id) {

        GetOrderDetailDTO order = orderService.getById(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(order, false));
    }

    /**
     * PUT /api/orders/{id}
     * Actualizar una orden (estado, mesa, etc)
     * Roles permitidos: WAITER, KITCHEN, ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_KITCHEN')")
    public ResponseEntity<ResponseDTO<String>> updateOrder(@PathVariable String id, @RequestBody @Valid UpdateOrderDTO updateOrderDTO) {

        orderService.update(id, updateOrderDTO);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Orden actualizada", false));
    }

    /**
     * PATCH /api/orders/{id}/cancel
     * Cancelar una orden
     * Roles permitidos: WAITER, ADMIN
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<String>> cancelOrder(@PathVariable String id) {

        orderService.cancel(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Orden cancelada", false));
    }

    /**
     * DELETE /api/orders/{id}
     * Eliminar una orden
     * Roles permitidos: ADMIN
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('order:delete', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> deleteOrder(@PathVariable String id) {

        orderService.delete(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Orden eliminada", false));
    }

    /**
     * DELETE /api/orders/{id}/abandon
     * Elimina una orden online pendiente de pago cuando el cliente abandona la pasarela.
     * Roles permitidos: CUSTOMER (solo sus propias órdenes)
     */
    @DeleteMapping("/{id}/abandon")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<String>> abandonOrder(@PathVariable String id) {

        orderService.abandonOrder(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>("Orden eliminada", false));
    }

    /**
     * GET /api/orders/me/{page}/page
     * Obtener las órdenes del cliente autenticado
     * Roles permitidos: CUSTOMER
     */
    @GetMapping("/me/{page}/page")
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<List<GetOrdersDTO>>> getMyOrders(@PathVariable int page) {

        List<GetOrdersDTO> orders = orderService.getMyOrders(page);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(orders, false));
    }

    /**
     * GET /api/orders/{id}/invoice
     * Obtener la factura asociada a una orden
     * Roles permitidos: WAITER, ADMIN, CUSTOMER
     */
    @GetMapping("/{id}/invoice")
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<GetInvoiceDTO>> getInvoiceByOrder(@PathVariable String id) {

        GetInvoiceDTO invoice = invoiceService.getInvoiceByOrderId(id);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(invoice, false));
    }
}
