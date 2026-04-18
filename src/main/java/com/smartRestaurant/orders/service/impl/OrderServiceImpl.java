package com.smartRestaurant.orders.service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.Recipe;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.Service.AdditionService;
import com.smartRestaurant.inventory.Service.DrinkService;
import com.smartRestaurant.inventory.Service.ProductService;
import com.smartRestaurant.inventory.dto.Product.StockMovementDTO;
import com.smartRestaurant.inventory.dto.drink.DrinkMovement;
import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.GetOrderDetailDTO;
import com.smartRestaurant.orders.dto.Order.GetOrdersDTO;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;
import com.smartRestaurant.orders.dto.orderitem.GetOrderItemDTO;
import com.smartRestaurant.orders.dto.invoice.CreateInvoiceDTO;
import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import com.smartRestaurant.orders.mapper.OrderMapper;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.OrderItem;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderPaymentStatus;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.inventory.util.CurrentUserProvider;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.OrderItemRepository;
import com.smartRestaurant.orders.service.InvoiceService;
import com.smartRestaurant.orders.service.OrderService;
import com.smartRestaurant.orders.service.SseService;
import com.smartRestaurant.restaurant.model.RestaurantTable;
import com.smartRestaurant.restaurant.model.enums.TableStatus;
import com.smartRestaurant.restaurant.repository.TableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartRestaurant.inventory.model.Product;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> VALID_TRANSITIONS = Map.of(
        OrderStatus.PENDING,     Set.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED),
        OrderStatus.IN_PROGRESS, Set.of(OrderStatus.COMPLETED,   OrderStatus.CANCELLED),
        OrderStatus.COMPLETED,   Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED,   Set.of(),
        OrderStatus.CANCELLED,   Set.of()
    );

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final DishRepository dishRepository;
    private final DrinkRepository drinkRepository;
    private final AdditionRepository additionRepository;
    private final InvoiceService invoiceService;
    private final CurrentUserProvider currentUserProvider;
    private final SseService sseService;
    private final TableRepository tableRepository;
    private final ProductService productService;
    private final DrinkService drinkService;
    private final AdditionService additionService;

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
        } else {
            // Si el usuario autenticado es mesero o admin creando presencial, asignarlo automáticamente
            User currentUser = currentUserProvider.getCurrentUser();
            if (currentUser != null && currentUser.getRole().isStaff()) {
                waiter = currentUser;
            }
        }

        Order order = orderMapper.toEntity(createOrderDto);
        order.setCustomer(customer);
        order.setWaiter(waiter);

        // Asignar y ocupar mesa solo en órdenes presenciales
        if (createOrderDto.channel() == OrderChannel.PRESENTIAL && createOrderDto.tableId() != null) {
            RestaurantTable table = tableRepository.findById(createOrderDto.tableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mesa no encontrada"));
            if (!table.isActive()) {
                throw new BadRequestException("La mesa " + table.getNumber() + " está inactiva");
            }
            if (table.getStatus() != TableStatus.FREE) {
                throw new BadRequestException("La mesa " + table.getNumber() + " no está disponible (estado: " + table.getStatus() + ")");
            }
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
            order.setTable(table);
        }

        // Establecer paymentStatus según channel
        if (createOrderDto.channel().equals(OrderChannel.ONLINE)) {
            order.setPaymentStatus(OrderPaymentStatus.PENDING);  // Necesita pago
            log.info(" Orden ONLINE - Requiere pago previo");
        } else {
            order.setPaymentStatus(OrderPaymentStatus.NOT_REQUIRED);  // Sin pago previo
            log.info(" Orden PRESENCIAL - Pago en el punto");
        }

        // Validar stock de ingredientes antes de persistir nada
        validateStockForOrderItems(createOrderDto.items());

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

        // Notificar cocina solo si la orden ya puede procesarse:
        // - Presencial: siempre (el mesero ya tomó el pedido)
        // - Online: solo si el pago ya está confirmado
        if (savedOrder.getPaymentStatus() != OrderPaymentStatus.PENDING) {
            sseService.notifyKitchen(buildListDTO(savedOrder));
        }

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

    /**
     * Valida que haya stock suficiente de ingredientes para todos los platos del pedido
     * antes de persistir cualquier dato. Acumula el total requerido por ingrediente
     * considerando todos los items juntos, luego compara contra el stock actual.
     * Lanza ValueConflictException (HTTP 409) listando todos los ingredientes faltantes.
     */
    private void validateStockForOrderItems(List<CreateOrderItemDTO> items) {
        // Acumular requerimientos por ingrediente (platos), bebida y adición
        Map<String, Double> ingredientsRequired = new HashMap<>();
        Map<String, Product> ingredientsById = new HashMap<>();

        Map<String, Integer> drinksRequired = new HashMap<>();
        Map<String, Drink> drinksById = new HashMap<>();

        Map<String, Integer> additionsRequired = new HashMap<>();
        Map<String, Addition> additionsById = new HashMap<>();

        for (CreateOrderItemDTO itemDto : items) {
            switch (itemDto.productType()) {
                case "DISH" -> {
                    Dish dish = dishRepository.findById(itemDto.productId())
                            .filter(d -> !d.getState().equals(State.INACTIVE))
                            .orElseThrow(() -> new ResourceNotFoundException("Plato no encontrado: " + itemDto.productId()));

                    if (dish.getRecipes() == null || dish.getRecipes().isEmpty()) break;

                    for (Recipe recipe : dish.getRecipes()) {
                        if (!State.ACTIVE.equals(recipe.getState())) continue;
                        Product ingredient = recipe.getProduct();
                        double required = recipe.getWeight() * itemDto.quantity();
                        ingredientsRequired.merge(ingredient.getId(), required, Double::sum);
                        ingredientsById.putIfAbsent(ingredient.getId(), ingredient);
                    }
                }
                case "DRINK" -> {
                    Drink drink = drinkRepository.findById(itemDto.productId())
                            .filter(d -> !d.getState().equals(State.INACTIVE))
                            .orElseThrow(() -> new ResourceNotFoundException("Bebida no encontrada: " + itemDto.productId()));
                    drinksRequired.merge(drink.getId(), itemDto.quantity(), Integer::sum);
                    drinksById.putIfAbsent(drink.getId(), drink);
                }
                case "ADDITION" -> {
                    Addition addition = additionRepository.findById(itemDto.productId())
                            .filter(a -> !a.getState().equals(State.INACTIVE))
                            .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada: " + itemDto.productId()));
                    additionsRequired.merge(addition.getId(), itemDto.quantity(), Integer::sum);
                    additionsById.putIfAbsent(addition.getId(), addition);
                }
            }
        }

        List<String> errors = new ArrayList<>();

        // Validar ingredientes de platos
        for (Map.Entry<String, Double> entry : ingredientsRequired.entrySet()) {
            Product ingredient = ingredientsById.get(entry.getKey());
            if (ingredient.getWeight() < entry.getValue()) {
                errors.add(String.format("ingrediente '%s' (disponible: %.1fg, requerido: %.1fg)",
                        ingredient.getName(), ingredient.getWeight(), entry.getValue()));
            }
        }

        // Validar bebidas
        for (Map.Entry<String, Integer> entry : drinksRequired.entrySet()) {
            Drink drink = drinksById.get(entry.getKey());
            if (drink.getUnits() < entry.getValue()) {
                errors.add(String.format("bebida '%s' (disponible: %d uds, requerido: %d uds)",
                        drink.getName(), drink.getUnits(), entry.getValue()));
            }
        }

        // Validar adiciones
        for (Map.Entry<String, Integer> entry : additionsRequired.entrySet()) {
            Addition addition = additionsById.get(entry.getKey());
            if (addition.getUnits() < entry.getValue()) {
                errors.add(String.format("adición '%s' (disponible: %d uds, requerido: %d uds)",
                        addition.getName(), addition.getUnits(), entry.getValue()));
            }
        }

        if (!errors.isEmpty()) {
            throw new ValueConflictException(
                    "Stock insuficiente para completar el pedido. Faltantes: " +
                    String.join(", ", errors)
            );
        }
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
    public List<GetOrdersDTO> getAll(int page, OrderStatus status, OrderChannel channel) {
        log.info("Obteniendo órdenes. Página: {}, status: {}, channel: {}", page, status, channel);

        Pageable pageable = PageRequest.of(page, 10);

        Page<Order> orders;
        if (status != null && channel != null) {
            orders = orderRepository.findByStatusAndChannel(status, channel, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else if (channel != null) {
            orders = orderRepository.findByChannel(channel, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return orders.stream()
                .map(this::buildListDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GetOrderDetailDTO getById(String id) {
        log.info("Obteniendo orden: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + id));

        // Si es CUSTOMER, solo puede ver sus propias órdenes
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser != null && !currentUser.getRole().isStaff()) {
            if (order.getCustomer() == null || !order.getCustomer().getId().equals(currentUser.getId())) {
                // 404 en lugar de 403 para no revelar que la orden existe
                throw new ResourceNotFoundException("Orden no encontrada: " + id);
            }
        }

        return buildDetailDTO(order);
    }

    @Override
    public void update(String id, UpdateOrderDTO updateOrderDTO) {
        log.info(" [ORDER] Actualizando orden: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        validateTransition(order.getStatus(), updateOrderDTO.status());

        orderMapper.updateOrder(updateOrderDTO, order);

        // Liberar mesa cuando la orden es entregada
        if (updateOrderDTO.status().equals(OrderStatus.DELIVERED) && order.getTable() != null) {
            order.getTable().setStatus(TableStatus.FREE);
            tableRepository.save(order.getTable());
            log.info("[ORDER] Mesa {} liberada al marcar orden {} como DELIVERED",
                    order.getTable().getNumber(), id);
        }

        // Si se marca como COMPLETED, crear factura y descontar inventario
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
                invoiceService.createInvoice(invoiceDto);
                log.info("[ORDER] Factura creada automáticamente para orden: {}", id);
            } catch (Exception e) {
                log.error(" [ORDER] Error creando factura: {}", e.getMessage());
            }

            // Descontar inventario de ingredientes por cada plato de la orden
            discountInventoryForOrder(order);

            // Notificar al mesero que el pedido está listo para recoger
            sseService.notifyWaiterOrderReady(buildListDTO(order));

            // Notificar al cliente si es ONLINE (ya pagó, está esperando)
            if (order.getChannel().equals(OrderChannel.ONLINE) && order.getCustomer() != null) {
                sseService.notifyCustomerOrderReady(order.getCustomer().getId(), buildListDTO(order));
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

    /**
     * Descuenta del inventario los ingredientes consumidos por cada item de la orden.
     * Solo procesa Dish (platos): cada receta activa define el ingrediente y el peso por porción.
     * Los errores por stock insuficiente se registran en el log sin bloquear la transición,
     * ya que el plato ya fue preparado por cocina.
     */
    private void discountInventoryForOrder(Order order) {
        log.info("[INVENTORY] Iniciando descuento de inventario para orden: {}", order.getId());

        for (OrderItem item : order.getItems()) {
            Object producto = item.getProducto();

            if (producto instanceof Dish dish) {
                List<Recipe> recipes = dish.getRecipes();

                if (recipes == null || recipes.isEmpty()) {
                    log.warn("[INVENTORY] El plato '{}' no tiene recetas definidas — sin descuento de ingredientes",
                            dish.getName());
                    continue;
                }

                for (Recipe recipe : recipes) {
                    if (!State.ACTIVE.equals(recipe.getState())) {
                        continue;
                    }

                    double totalWeight = recipe.getWeight() * item.getQuantity();
                    String productId = recipe.getProduct().getId();
                    String productName = recipe.getProduct().getName();
                    String reason = String.format("Orden #%s — plato '%s' x%d — ingrediente '%s' (%.1fg/ud)",
                            order.getId(), dish.getName(), item.getQuantity(),
                            productName, recipe.getWeight());

                    try {
                        productService.discountStock(productId, new StockMovementDTO(totalWeight, reason));
                        log.info("[INVENTORY] Descontado: {}g de '{}' (plato: '{}', cantidad: {})",
                                totalWeight, productName, dish.getName(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("[INVENTORY] No se pudo descontar {}g de '{}' para orden {}: {}",
                                totalWeight, productName, order.getId(), e.getMessage());
                    }
                }
            } else if (producto instanceof Drink drink) {
                try {
                    drinkService.discountStock(drink.getId(), new DrinkMovement(item.getQuantity()));
                    log.info("[INVENTORY] Descontado: {} unidad(es) de bebida '{}' (cantidad: {})",
                            item.getQuantity(), drink.getName(), item.getQuantity());
                } catch (Exception e) {
                    log.error("[INVENTORY] No se pudo descontar {} unidad(es) de bebida '{}' para orden {}: {}",
                            item.getQuantity(), drink.getName(), order.getId(), e.getMessage());
                }

            } else if (producto instanceof Addition addition) {
                try {
                    additionService.discountStock(addition.getId(), new DrinkMovement(item.getQuantity()));
                    log.info("[INVENTORY] Descontado: {} unidad(es) de adición '{}' (cantidad: {})",
                            item.getQuantity(), addition.getName(), item.getQuantity());
                } catch (Exception e) {
                    log.error("[INVENTORY] No se pudo descontar {} unidad(es) de adición '{}' para orden {}: {}",
                            item.getQuantity(), addition.getName(), order.getId(), e.getMessage());
                }
            }
        }

        log.info("[INVENTORY] Descuento de inventario finalizado para orden: {}", order.getId());
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

        // Liberar mesa al cancelar
        if (order.getTable() != null) {
            order.getTable().setStatus(TableStatus.FREE);
            tableRepository.save(order.getTable());
            log.info("[ORDER] Mesa {} liberada al cancelar orden {}", order.getTable().getNumber(), id);
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

    @Override
    public void abandonOrder(String orderId) {
        log.info("[ORDER] Cliente abandona pasarela de pago. Eliminando orden: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        // Solo se puede abandonar una orden online que aún no fue pagada
        if (!order.getChannel().equals(OrderChannel.ONLINE)) {
            throw new BadRequestException("Solo se pueden abandonar órdenes en línea");
        }
        if (!order.getStatus().equals(OrderStatus.PENDING) ||
            !order.getPaymentStatus().equals(OrderPaymentStatus.PENDING)) {
            throw new BadRequestException("La orden ya fue procesada y no puede eliminarse");
        }

        // Verificar que el cliente autenticado es el dueño de la orden
        User currentUser = currentUserProvider.getCurrentUser();
        if (currentUser != null && order.getCustomer() != null &&
            !order.getCustomer().getId().equals(currentUser.getId())) {
            throw new BadRequestException("No tienes permiso para abandonar esta orden");
        }

        orderRepository.deleteById(orderId);
        log.info("[ORDER] Orden {} eliminada por abandono de pasarela de pago", orderId);
    }

    // =====================================================================
    // BUILDERS DE RESPUESTA — construyen los DTOs con todos los campos
    // =====================================================================

    private GetOrdersDTO buildListDTO(Order order) {
        int itemCount   = order.getItems() != null ? order.getItems().size() : 0;
        double total    = order.getItems() != null
                ? order.getItems().stream().mapToDouble(this::getPriceOfItem).sum() : 0.0;
        String customer = order.getCustomer() != null
                ? order.getCustomer().getFullName() : "Presencial";

        return new GetOrdersDTO(
                order.getId(),
                order.getStatus(),
                order.getChannel(),
                customer,
                order.getCreatedAt(),
                itemCount,
                total
        );
    }

    private GetOrderDetailDTO buildDetailDTO(Order order) {
        List<GetOrderItemDTO> items = order.getItems() != null
                ? order.getItems().stream().map(this::buildItemDTO).toList()
                : List.of();

        double total    = items.stream().mapToDouble(GetOrderItemDTO::totalPrice).sum();
        String customer = order.getCustomer() != null
                ? order.getCustomer().getFullName() : "Presencial";
        String waiter   = order.getWaiter() != null
                ? order.getWaiter().getId().toString() : null;
        String paymentStatus = order.getPaymentStatus() != null
                ? order.getPaymentStatus().name() : null;

        GetOrderDetailDTO.TableInfo tableInfo = null;
        if (order.getTable() != null) {
            RestaurantTable t = order.getTable();
            tableInfo = new GetOrderDetailDTO.TableInfo(
                    t.getId(), t.getNumber(), t.getCapacity(), t.getLocation(), t.getStatus());
        }

        return new GetOrderDetailDTO(
                order.getId(),
                order.getStatus(),
                order.getChannel(),
                customer,
                waiter,
                tableInfo,
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items,
                total,
                paymentStatus
        );
    }

    private GetOrderItemDTO buildItemDTO(OrderItem item) {
        Object product = item.getProducto();

        String productId;
        String productName;
        String productType;
        double unitPrice;

        if (product instanceof Dish dish) {
            productId   = dish.getId();
            productName = dish.getName();
            productType = "DISH";
            unitPrice   = dish.getPrice();
        } else if (product instanceof Drink drink) {
            productId   = drink.getId();
            productName = drink.getName();
            productType = "DRINK";
            unitPrice   = drink.getPrice();
        } else if (product instanceof Addition addition) {
            productId   = addition.getId();
            productName = addition.getName();
            productType = "ADDITION";
            unitPrice   = addition.getPrice();
        } else {
            productId   = "";
            productName = "Desconocido";
            productType = "UNKNOWN";
            unitPrice   = 0.0;
        }

        return new GetOrderItemDTO(
                item.getId(),
                productId,
                productName,
                productType,
                item.getQuantity(),
                unitPrice,
                unitPrice * item.getQuantity(),
                item.getNotes()
        );
    }

    private void validateTransition(OrderStatus current, OrderStatus next) {
        Set<OrderStatus> allowed = VALID_TRANSITIONS.get(current);
        if (!allowed.contains(next)) {
            throw new BadRequestException(
                "Transición no permitida: " + current + " → " + next +
                ". Desde " + current + " solo se puede ir a: " + allowed
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetOrdersDTO> getMyOrders(int page) {
        User currentUser = currentUserProvider.getCurrentUser();

        if (currentUser == null) {
            throw new BadRequestException("No hay usuario autenticado");
        }

        log.info("Obteniendo órdenes del cliente: {}", currentUser.getEmail());

        Pageable pageable = PageRequest.of(page, 10);
        Page<Order> orders = orderRepository.findByCustomer(currentUser, pageable);

        return orders.stream()
                .map(this::buildListDTO)
                .toList();
    }
}
