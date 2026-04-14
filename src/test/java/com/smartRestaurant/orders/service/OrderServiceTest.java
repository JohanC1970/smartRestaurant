package com.smartRestaurant.orders.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.Repository.AdditionRepository;
import com.smartRestaurant.inventory.Repository.DishRepository;
import com.smartRestaurant.inventory.Repository.DrinkRepository;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.model.Dish;
import com.smartRestaurant.inventory.model.State;
import com.smartRestaurant.inventory.util.CurrentUserProvider;
import com.smartRestaurant.orders.dto.Order.CreateOrderDto;
import com.smartRestaurant.orders.dto.Order.UpdateOrderDTO;
import com.smartRestaurant.orders.dto.orderitem.CreateOrderItemDTO;
import com.smartRestaurant.orders.mapper.OrderMapper;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderPaymentStatus;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.orders.repository.OrderItemRepository;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private UserRepository userRepository;
    @Mock private DishRepository dishRepository;
    @Mock private DrinkRepository drinkRepository;
    @Mock private AdditionRepository additionRepository;
    @Mock private InvoiceService invoiceService;
    @Mock private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId("order-1");
        testOrder.setItems(new ArrayList<>());
    }

    // =====================================================================
    // MÁQUINA DE ESTADOS
    // Cada test verifica que una transición es válida o inválida.
    // El patrón es siempre: setear el estado actual, llamar update() con
    // el estado nuevo, y verificar si lanza excepción o no.
    // =====================================================================

    @Test
    void update_PendingToInProgress_Success() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        assertDoesNotThrow(() ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.IN_PROGRESS, null, null))
        );
    }

    @Test
    void update_PendingToCancelled_Success() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        assertDoesNotThrow(() ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.CANCELLED, null, null))
        );
    }

    @Test
    void update_InProgressToCompleted_Success() {
        testOrder.setStatus(OrderStatus.IN_PROGRESS);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        assertDoesNotThrow(() ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.COMPLETED, null, null))
        );
    }

    @Test
    void update_CompletedToDelivered_Success() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        assertDoesNotThrow(() ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.DELIVERED, null, null))
        );
    }

    @Test
    void update_PendingToDelivered_ThrowsBadRequest() {
        // No se puede saltar la cocina
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.DELIVERED, null, null))
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    void update_CompletedToInProgress_ThrowsBadRequest() {
        // No se puede retroceder
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.IN_PROGRESS, null, null))
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    void update_DeliveredToAny_ThrowsBadRequest() {
        // Estado final, no tiene salida
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.PENDING, null, null))
        );
        verify(orderRepository, never()).save(any());
    }

    @Test
    void update_CancelledToAny_ThrowsBadRequest() {
        // Estado final, no tiene salida
        testOrder.setStatus(OrderStatus.CANCELLED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () ->
            orderService.update("order-1", new UpdateOrderDTO(OrderStatus.PENDING, null, null))
        );
        verify(orderRepository, never()).save(any());
    }

    // =====================================================================
    // CANCEL
    // =====================================================================

    @Test
    void cancel_PendingOrder_SetsStatusCancelled() {
        testOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any())).thenReturn(testOrder);

        orderService.cancel("order-1");

        assertEquals(OrderStatus.CANCELLED, testOrder.getStatus());
        verify(orderRepository).save(testOrder);
    }

    @Test
    void cancel_CompletedOrder_ThrowsBadRequest() {
        testOrder.setStatus(OrderStatus.COMPLETED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancel("order-1"));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancel_DeliveredOrder_ThrowsBadRequest() {
        testOrder.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () -> orderService.cancel("order-1"));
        verify(orderRepository, never()).save(any());
    }

    // =====================================================================
    // CREATE
    // =====================================================================

    @Test
    void create_WithNullChannel_ThrowsBadRequest() {
        CreateOrderDto dto = new CreateOrderDto(
            null, null, null, null,
            List.of(new CreateOrderItemDTO("dish-1", "DISH", 1, null))
        );

        assertThrows(BadRequestException.class, () -> orderService.create(dto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_WithEmptyItems_ThrowsBadRequest() {
        CreateOrderDto dto = new CreateOrderDto(
            OrderChannel.ONLINE, null, null, null, List.of()
        );

        assertThrows(BadRequestException.class, () -> orderService.create(dto));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void create_OnlineOrder_SetsPendingPaymentStatus() {
        // El mapper retorna una orden sin paymentStatus seteado
        Order orderFromMapper = new Order();
        orderFromMapper.setId("order-1");
        orderFromMapper.setItems(new ArrayList<>());

        Dish dish = new Dish();
        dish.setId("dish-1");
        dish.setState(State.ACTIVE);

        CreateOrderDto dto = new CreateOrderDto(
            OrderChannel.ONLINE, 1L, null, null,
            List.of(new CreateOrderItemDTO("dish-1", "DISH", 2, null))
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(orderMapper.toEntity(dto)).thenReturn(orderFromMapper);
        when(orderRepository.save(any())).thenReturn(orderFromMapper);
        when(dishRepository.findById("dish-1")).thenReturn(Optional.of(dish));

        orderService.create(dto);

        // El servicio debe asignar PENDING porque es ONLINE
        assertEquals(OrderPaymentStatus.PENDING, orderFromMapper.getPaymentStatus());
    }
}
