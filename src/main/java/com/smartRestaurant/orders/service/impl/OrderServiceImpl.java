package com.smartRestaurant.orders.service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.ValueConflictException;
import com.smartRestaurant.inventory.model.Addition;
import com.smartRestaurant.inventory.model.AdditionRecipe;
import com.smartRestaurant.inventory.model.AdditionType;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.Drink;
import com.smartRestaurant.inventory.model.DrinkRecipe;
import com.smartRestaurant.inventory.model.DrinkType;
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
import com.smartRestaurant.orders.dto.Order.EditOrderItemsDTO;
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
        OrderStatus.PENDING,     Set.of(OrderStatus.SENT,        OrderStatus.CANCELLED),
        OrderStatus.SENT,        Set.of(OrderStatus.IN_PROGRESS, OrderStatus.CANCELLED),
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

        // Para órdenes ONLINE con pago ya confirmado, notificar cocina inmediatamente.
        // Para órdenes PRESENCIALES, la notificación a cocina ocurre cuando el mesero
        // cambia el estado a SENT (PENDING → SENT).
        if (savedOrder.getChannel().equals(OrderChannel.ONLINE) &&
            savedOrder.getPaymentStatus() != OrderPaymentStatus.PENDING) {
            log.info(" [ORDER] Orden ONLINE con pago confirmado — notificando cocina: {}", savedOrder.getId());
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

        // Congelar precio al momento del pedido — no debe depender del precio actual del catálogo
        double frozenPrice = switch (producto) {
            case Dish dish       -> dish.getPrice();
            case Drink drink     -> drink.getSalePrice();
            case Addition add    -> add.getSalePrice();
            default              -> 0.0;
        };
        orderItem.setUnitPrice(frozenPrice);

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

                    if (dish.getRecipes() == null || dish.getRecipes().isEmpty()) {
                        throw new ValueConflictException(
                            "El plato '" + dish.getName() + "' no tiene recetas configuradas. " +
                            "Contacte al administrador para configurar los ingredientes.");
                    }

                    boolean hasActiveRecipes = dish.getRecipes().stream()
                            .anyMatch(r -> State.ACTIVE.equals(r.getState()));
                    if (!hasActiveRecipes) {
                        throw new ValueConflictException(
                            "El plato '" + dish.getName() + "' no tiene recetas activas configuradas. " +
                            "Contacte al administrador.");
                    }

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

                    if (drink.getDrinkType() == DrinkType.SIMPLE) {
                        drinksRequired.merge(drink.getId(), itemDto.quantity(), Integer::sum);
                        drinksById.putIfAbsent(drink.getId(), drink);
                    } else {
                        // PREPARED: valida disponibilidad de ingredientes, igual que platos
                        if (drink.getRecipes() == null || drink.getRecipes().isEmpty()) {
                            throw new ValueConflictException(
                                "La bebida preparada '" + drink.getName() + "' no tiene recetas configuradas. " +
                                "Contacte al administrador para configurar los ingredientes.");
                        }
                        boolean hasActiveRecipes = drink.getRecipes().stream()
                                .anyMatch(r -> State.ACTIVE.equals(r.getState()));
                        if (!hasActiveRecipes) {
                            throw new ValueConflictException(
                                "La bebida preparada '" + drink.getName() + "' no tiene recetas activas.");
                        }
                        for (DrinkRecipe recipe : drink.getRecipes()) {
                            if (!State.ACTIVE.equals(recipe.getState())) continue;
                            Product ingredient = recipe.getProduct();
                            double required = recipe.getWeight() * itemDto.quantity();
                            ingredientsRequired.merge(ingredient.getId(), required, Double::sum);
                            ingredientsById.putIfAbsent(ingredient.getId(), ingredient);
                        }
                    }
                }
                case "ADDITION" -> {
                    Addition addition = additionRepository.findById(itemDto.productId())
                            .filter(a -> !a.getState().equals(State.INACTIVE))
                            .orElseThrow(() -> new ResourceNotFoundException("Adición no encontrada: " + itemDto.productId()));

                    if (addition.getAdditionType() == AdditionType.SIMPLE) {
                        additionsRequired.merge(addition.getId(), itemDto.quantity(), Integer::sum);
                        additionsById.putIfAbsent(addition.getId(), addition);
                    } else {
                        // PREPARED: valida disponibilidad de ingredientes, igual que platos
                        if (addition.getRecipes() == null || addition.getRecipes().isEmpty()) {
                            throw new ValueConflictException(
                                "La adición preparada '" + addition.getName() + "' no tiene recetas configuradas. " +
                                "Contacte al administrador para configurar los ingredientes.");
                        }
                        boolean hasActiveRecipes = addition.getRecipes().stream()
                                .anyMatch(r -> State.ACTIVE.equals(r.getState()));
                        if (!hasActiveRecipes) {
                            throw new ValueConflictException(
                                "La adición preparada '" + addition.getName() + "' no tiene recetas activas.");
                        }
                        for (AdditionRecipe recipe : addition.getRecipes()) {
                            if (!State.ACTIVE.equals(recipe.getState())) continue;
                            Product ingredient = recipe.getProduct();
                            double required = recipe.getWeight() * itemDto.quantity();
                            ingredientsRequired.merge(ingredient.getId(), required, Double::sum);
                            ingredientsById.putIfAbsent(ingredient.getId(), ingredient);
                        }
                    }
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
        // Para el cajero: cuando se filtra por DELIVERED, excluir las ya cobradas (CONFIRMED)
        boolean excludePaid = OrderStatus.DELIVERED.equals(status);
        if (status != null && channel != null) {
            orders = excludePaid
                ? orderRepository.findByStatusAndChannelAndPaymentStatusNot(status, channel, OrderPaymentStatus.CONFIRMED, pageable)
                : orderRepository.findByStatusAndChannel(status, channel, pageable);
        } else if (status != null) {
            orders = excludePaid
                ? orderRepository.findByStatusAndPaymentStatusNot(status, OrderPaymentStatus.CONFIRMED, pageable)
                : orderRepository.findByStatus(status, pageable);
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

        // Liberar mesa cuando la orden es entregada manualmente (sin pago — edge case)
        // El caso principal de liberación es en payPresentialInvoice() cuando se confirma el pago
        if (updateOrderDTO.status().equals(OrderStatus.DELIVERED) && order.getTable() != null) {
            order.getTable().setStatus(TableStatus.FREE);
            tableRepository.save(order.getTable());
            log.info("[ORDER] Mesa {} liberada al marcar orden {} como DELIVERED",
                    order.getTable().getNumber(), id);
        }

        // Al enviar a cocina (PENDING → SENT): notificar a la estación de cocina
        if (updateOrderDTO.status().equals(OrderStatus.SENT)) {
            log.info(" [ORDER] Orden enviada a cocina: {}", id);
            sseService.notifyKitchen(buildListDTO(order));
        }

        // Cuando el mesero entrega el pedido (COMPLETED → DELIVERED): notificar al cajero
        if (updateOrderDTO.status().equals(OrderStatus.DELIVERED)) {
            log.info(" [ORDER] Orden entregada, notificando cajero: {}", id);
            sseService.notifyCashierOrderReadyToPay(buildListDTO(order));
        }

        // Si se marca como COMPLETED (cocina terminó), crear factura y descontar inventario
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

            // Notificar al cliente (ONLINE u ONLINE con customer asignado)
            if (order.getCustomer() != null) {
                sseService.notifyCustomerOrderReady(order.getCustomer().getId(), buildListDTO(order));
            }
        }

        orderRepository.save(order);

        // Notificar al cliente cualquier cambio de estado (excepto COMPLETED, ya notificado arriba)
        if (!updateOrderDTO.status().equals(OrderStatus.COMPLETED) && order.getCustomer() != null) {
            sseService.notifyCustomerOrderStatusChanged(
                order.getCustomer().getId(),
                updateOrderDTO.status().name(),
                buildListDTO(order)
            );
        }
    }
    
    /**
     * Obtiene el precio del producto en un OrderItem usando el precio congelado al momento del pedido.
     * Nunca debe leer el precio actual del catálogo para evitar inconsistencias retroactivas.
     */
    private double getPriceOfItem(OrderItem item) {
        return item.getUnitPrice() * item.getQuantity();
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
                        productService.discountStock(productId, new StockMovementDTO(totalWeight, null, reason));
                        log.info("[INVENTORY] Descontado: {}g de '{}' (plato: '{}', cantidad: {})",
                                totalWeight, productName, dish.getName(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("[INVENTORY] No se pudo descontar {}g de '{}' para orden {}: {}",
                                totalWeight, productName, order.getId(), e.getMessage());
                    }
                }
            } else if (producto instanceof Drink drink) {
                if (drink.getDrinkType() == DrinkType.SIMPLE) {
                    try {
                        drinkService.discountStock(drink.getId(), new DrinkMovement(item.getQuantity()));
                        log.info("[INVENTORY] Descontado: {} unidad(es) de bebida simple '{}' (cantidad: {})",
                                item.getQuantity(), drink.getName(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("[INVENTORY] No se pudo descontar {} unidad(es) de bebida '{}' para orden {}: {}",
                                item.getQuantity(), drink.getName(), order.getId(), e.getMessage());
                    }
                } else {
                    // PREPARED: descontar ingredientes del inventario, igual que platos
                    if (drink.getRecipes() != null) {
                        for (DrinkRecipe recipe : drink.getRecipes()) {
                            if (!State.ACTIVE.equals(recipe.getState())) continue;
                            double totalWeight = recipe.getWeight() * item.getQuantity();
                            String productId = recipe.getProduct().getId();
                            String productName = recipe.getProduct().getName();
                            String reason = String.format(
                                "Orden #%s — bebida preparada '%s' x%d — ingrediente '%s' (%.1fg/ud)",
                                order.getId(), drink.getName(), item.getQuantity(),
                                productName, recipe.getWeight());
                            try {
                                productService.discountStock(productId, new StockMovementDTO(totalWeight, null, reason));
                                log.info("[INVENTORY] Descontado: {}g de '{}' para bebida preparada '{}' (x{})",
                                        totalWeight, productName, drink.getName(), item.getQuantity());
                            } catch (Exception e) {
                                log.error("[INVENTORY] No se pudo descontar {}g de '{}' para bebida '{}' en orden {}: {}",
                                        totalWeight, productName, drink.getName(), order.getId(), e.getMessage());
                            }
                        }
                    }
                }

            } else if (producto instanceof Addition addition) {
                if (addition.getAdditionType() == AdditionType.SIMPLE) {
                    try {
                        additionService.discountStock(addition.getId(), new DrinkMovement(item.getQuantity()));
                        log.info("[INVENTORY] Descontado: {} unidad(es) de adición simple '{}' (cantidad: {})",
                                item.getQuantity(), addition.getName(), item.getQuantity());
                    } catch (Exception e) {
                        log.error("[INVENTORY] No se pudo descontar {} unidad(es) de adición '{}' para orden {}: {}",
                                item.getQuantity(), addition.getName(), order.getId(), e.getMessage());
                    }
                } else {
                    // PREPARED: descontar ingredientes del inventario, igual que platos
                    if (addition.getRecipes() != null) {
                        for (AdditionRecipe recipe : addition.getRecipes()) {
                            if (!State.ACTIVE.equals(recipe.getState())) continue;
                            double totalWeight = recipe.getWeight() * item.getQuantity();
                            String productId = recipe.getProduct().getId();
                            String productName = recipe.getProduct().getName();
                            String reason = String.format(
                                "Orden #%s — adición preparada '%s' x%d — ingrediente '%s' (%.1fg/ud)",
                                order.getId(), addition.getName(), item.getQuantity(),
                                productName, recipe.getWeight());
                            try {
                                productService.discountStock(productId, new StockMovementDTO(totalWeight, null, reason));
                                log.info("[INVENTORY] Descontado: {}g de '{}' para adición preparada '{}' (x{})",
                                        totalWeight, productName, addition.getName(), item.getQuantity());
                            } catch (Exception e) {
                                log.error("[INVENTORY] No se pudo descontar {}g de '{}' para adición '{}' en orden {}: {}",
                                        totalWeight, productName, addition.getName(), order.getId(), e.getMessage());
                            }
                        }
                    }
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
            throw new BadRequestException("No se puede cancelar una orden en estado " + order.getStatus());
        }

        // Cancelar desde IN_PROGRESS implica desperdicio — se registra en el log para auditoría
        if (order.getStatus().equals(OrderStatus.IN_PROGRESS)) {
            log.warn("[ORDER] Cancelación de orden en preparación: {} — posible desperdicio de ingredientes", id);
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
                total,
                order.getPaymentStatus()
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

        if (product instanceof Dish dish) {
            productId   = dish.getId();
            productName = dish.getName();
            productType = "DISH";
        } else if (product instanceof Drink drink) {
            productId   = drink.getId();
            productName = drink.getName();
            productType = "DRINK";
        } else if (product instanceof Addition addition) {
            productId   = addition.getId();
            productName = addition.getName();
            productType = "ADDITION";
        } else {
            productId   = "";
            productName = "Desconocido";
            productType = "UNKNOWN";
        }

        // Usar precio congelado al momento del pedido
        double unitPrice = item.getUnitPrice();

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
    public void editItems(String id, EditOrderItemsDTO dto) {
        log.info("[ORDER] Editando items de orden: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if (!order.getStatus().equals(OrderStatus.PENDING)) {
            throw new BadRequestException("Solo se pueden editar items de órdenes PENDIENTES");
        }

        validateStockForOrderItems(dto.items());

        // Reemplazar items — orphanRemoval elimina los anteriores al guardar
        order.getItems().clear();

        List<OrderItem> newItems = new ArrayList<>();
        for (CreateOrderItemDTO itemDto : dto.items()) {
            OrderItem item = new OrderItem();
            item.setId(UUID.randomUUID().toString());
            item.setNotes(itemDto.notes());
            item.setOrder(order);
            item.setQuantity(itemDto.quantity());
            item.setProducto(loadProductByType(itemDto.productType(), itemDto.productId()));
            newItems.add(item);
        }

        order.getItems().addAll(newItems);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("[ORDER] Items actualizados para orden {}: {} items", id, newItems.size());
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
