package com.smartRestaurant.orders.service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.GetOrderDetailDTO;
import com.smartRestaurant.orders.dto.Order.GetOrdersDTO;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;
import com.smartRestaurant.orders.dto.invoice.CreateInvoiceDTO;
import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import com.smartRestaurant.orders.mapper.OrderMapper;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.OrderItem;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderPaymentStatus;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.OrderItemRepository;
import com.smartRestaurant.orders.service.InvoiceService;
import com.smartRestaurant.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final DrinkRepository drinkRepository;
    private final AdditionRepository additionRepository;
    private final InvoiceService invoiceService;

    @Override
    public String create(CreateOrderDto createOrderDto) {
        log.info(" [ORDER] Creando nueva orden. Canal: {}, Items: {}",
                 createOrderDto.channel(), createOrderDto.items().size());

        validateCreateOrderDto(createOrderDto);

        User customer = null;
        if (createOrderDto.customerId() != null) {
            customer = userRepository.findById(createOrderDto.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        }

        User waiter = null;
        if (createOrderDto.waiterId() != null) {
            waiter = userRepository.findById(createOrderDto.waiterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mesero no encontrado"));
        }

        Order order = orderMapper.toEntity(createOrderDto);
        order.setCustomer(customer);
        order.setWaiter(waiter);
        order.setTableNumber(createOrderDto.tableNumber());

        // NUEVO: Establecer paymentStatus según channel
        if (createOrderDto.channel().equals(OrderChannel.ONLINE)) {
            order.setPaymentStatus(OrderPaymentStatus.PENDING);  // Necesita pago
            log.info(" Orden ONLINE - Requiere pago previo");
        } else {
            order.setPaymentStatus(OrderPaymentStatus.NOT_REQUIRED);  // Sin pago previo
            log.info(" Orden PRESENCIAL - Pago en el punto");
        }

        List<OrderItem> items = new ArrayList<>();

        orderRepository.save(order);

        log.info(" Intento de crear lista de items");

        for(CreateOrderItemDTO itemDto : createOrderDto.items()) {
            OrderItem orderItem = createOrderItem(itemDto, order);
            items.add(orderItem);
        }

        log.info(" lista de items creados ");
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        
        log.info(" [ORDER] Orden creada: {}, Total items: {}, Estado pago: {}",
                 savedOrder.getId(), items.size(), savedOrder.getPaymentStatus());

        return savedOrder.getId();
    }

    private OrderItem createOrderItem(CreateOrderItemDTO itemDto, Order order) {
        String itemId = UUID.randomUUID().toString();
        
        OrderItem orderItem = new OrderItem();
        orderItem.setId(itemId);
        orderItem.setNotes(itemDto.notes());
        orderItem.setOrder(order);
        orderItem.setQuantity(itemDto.quantity());

        log.info(" Intento de cargar producto: {}", itemDto.productId());

        Object producto = loadProductByType(itemDto.productType(), itemDto.productId());
        orderItem.setProducto(producto);

        log.info(" Producto cargado: {}", producto.getClass().getSimpleName()+ "intento de guardar: {}"+ order.getId());

        orderItemRepository.save(orderItem);
        log.info(" [ORDER] Item creado: {}", itemId);
        return orderItem;
    }

    private Object loadProductByType(String productType, String productId) {
        return switch (productType) {
            case "DISH" -> dishRepository.findById(productId)
                    .filter(dish -> !dish.getState().equals(State.INACTIVE))
                    .orElseThrow(() -> new ResourceNotFoundException("Dish no encontrado"));
            
            case "DRINK" -> drinkRepository.findById(productId)
                    .filter(drink -> !drink.getState().equals(State.INACTIVE))
                    .orElseThrow(() -> new ResourceNotFoundException("Drink no encontrada"));
            
            case "ADDITION" -> additionRepository.findById(productId)
                    .filter(addition -> !addition.getState().equals(State.INACTIVE))
                    .orElseThrow(() -> new ResourceNotFoundException("Addition no encontrada"));
            
            default -> throw new BadRequestException("Tipo no válido: " + productType);
        };
    }

    private void validateCreateOrderDto(CreateOrderDto orderDto) {
        if (orderDto.channel() == null) {
            throw new BadRequestException("El canal es obligatorio");
        }

        if (orderDto.items() == null || orderDto.items().isEmpty()) {
            throw new BadRequestException("Debe tener al menos un item");
        }

        for (CreateOrderItemDTO item : orderDto.items()) {
            if (item.productId() == null || item.productId().isEmpty()) {
                throw new BadRequestException("ProductID obligatorio");
            }
            if (item.productType() == null || item.productType().isEmpty()) {
                throw new BadRequestException("ProductType obligatorio");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetOrdersDTO> getAll(int page) {
        log.info("Obteniendo órdenes. Página: {}", page);

        Pageable pageable = PageRequest.of(page, 10);
        Page<Order> orders = orderRepository.findAll(pageable);

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No hay órdenes");
        }

        return orders.stream()
                .map(orderMapper::toListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderDetailDTO getById(String id) {
        log.info("Obteniendo orden: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));

        return orderMapper.toDetailDTO(order);
    }

    @Override
    public void update(String id, UpdateOrderDTO updateOrderDTO) {
        log.info(" [ORDER] Actualizando orden: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if(order.getStatus().equals(OrderStatus.COMPLETED) || order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new BadRequestException("No se puede actualizar una orden COMPLETED o DELIVERED");
        }

        orderMapper.updateOrder(updateOrderDTO, order);

        // NUEVO: Si se marca como COMPLETED, crear factura automáticamente
        if (updateOrderDTO.status().equals(OrderStatus.COMPLETED)) {
            log.info(" [ORDER] Orden completada, generando factura automáticamente: {}", id);
            
            // Calcular totales de items
            double subtotal = order.getItems().stream()
                .mapToDouble(this::getPriceOfItem)
                .sum();
            
            double tax = subtotal * 0.21;  // IVA 21% colombia 2026
            
            // Crear DTO
            CreateInvoiceDTO invoiceDto = new CreateInvoiceDTO(
                id,
                subtotal,
                tax
            );
            
            try {
                // Llamar a servicio para crear factura
                invoiceService.createInvoice(invoiceDto);
                log.info("[ORDER] Factura creada automáticamente para orden: {}", id);
            } catch (Exception e) {
                log.error(" [ORDER] Error creando factura: {}", e.getMessage());
                // No fallar la actualización de orden si falla la factura
            }
        }
        
        orderRepository.save(order);
    }
    
    /**
     * Obtiene el precio del producto en un OrderItem
     */
    private double getPriceOfItem(OrderItem item) {
        Object producto = item.getProducto();
        
        if (producto instanceof Dish dish) {
            try {
                return dish.getPrice()*item.getQuantity();
            } catch (NumberFormatException e) {
                log.warn(" Precio inválido para Dish {}: {}", dish.getId(), dish.getPrice());
                return 0.0;
            }
        } else if (producto instanceof Addition addition) {
            return addition.getPrice()*item.getQuantity();

        } else if (producto instanceof Drink drink) {
            return drink.getPrice()*item.getQuantity();
        }

        return 0.0;
    }

    @Override
    public void cancel(String id) {
        log.info("Cancelando orden: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if (order.getStatus().equals(OrderStatus.COMPLETED) || 
            order.getStatus().equals(OrderStatus.DELIVERED)) {
            throw new BadRequestException("No se puede cancelar");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void delete(String id) {
        log.info("Eliminando orden: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden no encontrada");
        }

        orderRepository.deleteById(id);
    }
}
