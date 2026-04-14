package com.smartRestaurant.orders.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.mapper.PaymentMapper;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.Payment;
import com.smartRestaurant.orders.model.enums.PaymentMethodType;
import com.smartRestaurant.orders.model.enums.PaymentStatus;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.PaymentRepository;
import com.smartRestaurant.orders.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentRepository paymentRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private PaymentMapper paymentMapper;
    @Mock private WompiPaymentClient wompiPaymentClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Order testOrder;
    private User testCustomer;
    private Payment testPayment;

    @BeforeEach
    void setUp() {
        testCustomer = new User();
        testCustomer.setId(1L);

        testOrder = new Order();
        testOrder.setId("order-1");
        testOrder.setPayment(null); // sin pago por defecto

        testPayment = new Payment();
        testPayment.setId("payment-1");
        testPayment.setAmount(60500);
        testPayment.setStatus(PaymentStatus.CONFIRMED);
        testPayment.setPaymentMethod(PaymentMethodType.WOMPI);
        testPayment.setTransactionId("wompi-tx-123");
    }

    // =====================================================================
    // CREATE PAYMENT
    // =====================================================================

    @Test
    void createPayment_WithValidData_ReturnsPaymentId() {
        CreatePaymentDTO dto = new CreatePaymentDTO(
            "order-1", 1L, 60500, "CASH", null, null
        );

        Payment mappedPayment = new Payment();
        mappedPayment.setId("payment-1");

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(paymentMapper.toEntity(dto)).thenReturn(mappedPayment);
        when(paymentRepository.save(any())).thenReturn(mappedPayment);

        String paymentId = paymentService.createPayment(dto);

        // El servicio sobreescribe el ID con UUID.randomUUID(), así que
        // solo verificamos que se generó un ID y que se guardó en BD
        assertNotNull(paymentId);
        verify(paymentRepository).save(any());
    }

    @Test
    void createPayment_WithNonExistentOrder_ThrowsNotFound() {
        CreatePaymentDTO dto = new CreatePaymentDTO(
            "orden-inexistente", 1L, 60500, "CASH", null, null
        );

        when(orderRepository.findById("orden-inexistente")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(dto));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_OrderAlreadyHasPayment_ThrowsBadRequest() {
        // La orden ya tiene un pago asociado — no se puede pagar dos veces
        testOrder.setPayment(testPayment);

        CreatePaymentDTO dto = new CreatePaymentDTO(
            "order-1", 1L, 60500, "CARD", null, null
        );

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));

        assertThrows(BadRequestException.class, () -> paymentService.createPayment(dto));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPayment_WithNonExistentCustomer_ThrowsNotFound() {
        CreatePaymentDTO dto = new CreatePaymentDTO(
            "order-1", 999L, 60500, "CASH", null, null
        );

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.createPayment(dto));
        verify(paymentRepository, never()).save(any());
    }

    // =====================================================================
    // REFUND WOMPI
    // =====================================================================

    @Test
    void refundWompiPayment_WithCashPayment_ThrowsBadRequest() {
        // Solo pagos de Wompi pueden reembolsarse
        testPayment.setPaymentMethod(PaymentMethodType.CASH);

        when(paymentRepository.findById("payment-1")).thenReturn(Optional.of(testPayment));

        assertThrows(BadRequestException.class,
            () -> paymentService.refundWompiPayment("payment-1"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refundWompiPayment_WithPendingPayment_ThrowsBadRequest() {
        // Solo pagos CONFIRMED pueden reembolsarse
        testPayment.setPaymentMethod(PaymentMethodType.WOMPI);
        testPayment.setStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById("payment-1")).thenReturn(Optional.of(testPayment));

        assertThrows(BadRequestException.class,
            () -> paymentService.refundWompiPayment("payment-1"));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refundWompiPayment_WithNonExistentPayment_ThrowsNotFound() {
        when(paymentRepository.findById("id-invalido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> paymentService.refundWompiPayment("id-invalido"));
    }

    // =====================================================================
    // GET PAYMENT
    // =====================================================================

    @Test
    void paymentDetail_WithNonExistentId_ThrowsNotFound() {
        when(paymentRepository.findById("id-invalido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> paymentService.paymentDetail("id-invalido"));
    }

    @Test
    void payments_WhenNoneExist_ThrowsNotFound() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.payments());
    }
}
