package com.smartRestaurant.orders.service;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.orders.dto.invoice.CreateInvoiceDTO;
import com.smartRestaurant.orders.dto.invoice.GetInvoiceDTO;
import com.smartRestaurant.orders.dto.payment.PayPresentialDTO;
import com.smartRestaurant.orders.mapper.InvoiceMapper;
import com.smartRestaurant.orders.mapper.PaymentMapper;
import com.smartRestaurant.orders.model.Invoice;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.Payment;
import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import com.smartRestaurant.orders.model.enums.OrderPaymentStatus;
import com.smartRestaurant.orders.model.enums.PaymentMethodType;
import com.smartRestaurant.orders.model.enums.PaymentStatus;
import com.smartRestaurant.orders.repository.InvoiceRepository;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.PaymentRepository;
import com.smartRestaurant.orders.service.impl.InvoiceServiceImpl;
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
class InvoiceServiceTest {

    @Mock private InvoiceRepository invoiceRepository;
    @Mock private OrderRepository orderRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private InvoiceMapper invoiceMapper;
    @Mock private PaymentMapper paymentMapper;
    @Mock private WompiPaymentClient wompiClient;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private Order testOrder;
    private Invoice testInvoice;
    private User testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new User();
        testCustomer.setId(1L);

        testOrder = new Order();
        testOrder.setId("order-1");
        testOrder.setCustomer(testCustomer);

        testInvoice = new Invoice();
        testInvoice.setId("INV-20260414-ABCDE");
        testInvoice.setStatus(InvoiceStatus.PENDING);
        testInvoice.setSubtotal(50000);
        testInvoice.setTax(10500);
        testInvoice.setTotal(60500);
        testInvoice.setOrder(testOrder);
    }

    // =====================================================================
    // CREATE INVOICE
    // =====================================================================

    @Test
    void createInvoice_WithValidOrder_ReturnsInvoiceId() {
        CreateInvoiceDTO dto = new CreateInvoiceDTO("order-1", 50000, 10500);

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(testOrder));
        when(invoiceMapper.toEntity(dto)).thenReturn(testInvoice);
        when(invoiceRepository.save(testInvoice)).thenReturn(testInvoice);
        when(orderRepository.save(testOrder)).thenReturn(testOrder);

        String invoiceId = invoiceService.createInvoice(dto);

        assertEquals("INV-20260414-ABCDE", invoiceId);
        // La orden debe quedar con paymentStatus PENDING
        assertEquals(OrderPaymentStatus.PENDING, testOrder.getPaymentStatus());
        verify(invoiceRepository).save(testInvoice);
    }

    @Test
    void createInvoice_WithNonExistentOrder_ThrowsNotFound() {
        CreateInvoiceDTO dto = new CreateInvoiceDTO("orden-inexistente", 50000, 10500);

        when(orderRepository.findById("orden-inexistente")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> invoiceService.createInvoice(dto));
        verify(invoiceRepository, never()).save(any());
    }

    // =====================================================================
    // PAY PRESENTIAL
    // Flujo: mesero registra pago en efectivo/tarjeta/transferencia
    // =====================================================================

    @Test
    void payPresentialInvoice_WithPendingInvoice_Success() {
        PayPresentialDTO dto = new PayPresentialDTO(
            "INV-20260414-ABCDE", PaymentMethodType.CASH, null, "Pagó en efectivo"
        );

        Payment savedPayment = new Payment();
        savedPayment.setId("payment-1");
        savedPayment.setStatus(PaymentStatus.CONFIRMED);

        GetInvoiceDTO invoiceDTO = new GetInvoiceDTO(
            "INV-20260414-ABCDE", "order-1", InvoiceStatus.PAID,
            50000, 10500, 60500, null, null, null
        );

        when(invoiceRepository.findById("INV-20260414-ABCDE")).thenReturn(Optional.of(testInvoice));
        when(paymentMapper.toEntity(dto)).thenReturn(new Payment());
        when(paymentRepository.save(any())).thenReturn(savedPayment);
        when(invoiceRepository.save(any())).thenReturn(testInvoice);
        when(orderRepository.save(any())).thenReturn(testOrder);
        when(invoiceMapper.toDTO(testInvoice)).thenReturn(invoiceDTO);

        GetInvoiceDTO result = invoiceService.payPresentialInvoice(dto);

        assertNotNull(result);
        // La factura debe quedar PAGADA
        assertEquals(InvoiceStatus.PAID, testInvoice.getStatus());
        verify(paymentRepository).save(any());
        verify(invoiceRepository).save(testInvoice);
    }

    @Test
    void payPresentialInvoice_WithAlreadyPaidInvoice_ThrowsBadRequest() {
        // La factura ya fue pagada — no se puede pagar dos veces
        testInvoice.setStatus(InvoiceStatus.PAID);

        PayPresentialDTO dto = new PayPresentialDTO(
            "INV-20260414-ABCDE", PaymentMethodType.CASH, null, null
        );

        when(invoiceRepository.findById("INV-20260414-ABCDE")).thenReturn(Optional.of(testInvoice));

        assertThrows(BadRequestException.class, () -> invoiceService.payPresentialInvoice(dto));
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void payPresentialInvoice_WithCancelledInvoice_ThrowsBadRequest() {
        // Una factura cancelada tampoco se puede pagar
        testInvoice.setStatus(InvoiceStatus.CANCELLED);

        PayPresentialDTO dto = new PayPresentialDTO(
            "INV-20260414-ABCDE", PaymentMethodType.CARD, null, null
        );

        when(invoiceRepository.findById("INV-20260414-ABCDE")).thenReturn(Optional.of(testInvoice));

        assertThrows(BadRequestException.class, () -> invoiceService.payPresentialInvoice(dto));
        verify(paymentRepository, never()).save(any());
    }

    // =====================================================================
    // CANCEL INVOICE
    // =====================================================================

    @Test
    void cancelInvoice_WithPendingInvoice_SetsCancelled() {
        when(invoiceRepository.findById("INV-20260414-ABCDE")).thenReturn(Optional.of(testInvoice));
        when(invoiceRepository.save(testInvoice)).thenReturn(testInvoice);

        invoiceService.cancelInvoice("INV-20260414-ABCDE");

        assertEquals(InvoiceStatus.CANCELLED, testInvoice.getStatus());
        verify(invoiceRepository).save(testInvoice);
    }

    @Test
    void cancelInvoice_WithPaidInvoice_ThrowsBadRequest() {
        // No se puede cancelar una factura que ya fue pagada
        testInvoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById("INV-20260414-ABCDE")).thenReturn(Optional.of(testInvoice));

        assertThrows(BadRequestException.class,
            () -> invoiceService.cancelInvoice("INV-20260414-ABCDE"));
        verify(invoiceRepository, never()).save(any());
    }

    // =====================================================================
    // GET INVOICE
    // =====================================================================

    @Test
    void getInvoice_WithNonExistentId_ThrowsNotFound() {
        when(invoiceRepository.findById("id-invalido")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> invoiceService.getInvoice("id-invalido"));
    }

    @Test
    void getAllInvoices_ReturnsListOfInvoices() {
        GetInvoiceDTO dto = new GetInvoiceDTO(
            "INV-20260414-ABCDE", "order-1", InvoiceStatus.PENDING,
            50000, 10500, 60500, null, null, null
        );

        when(invoiceRepository.findAll()).thenReturn(List.of(testInvoice));
        when(invoiceMapper.toDTO(testInvoice)).thenReturn(dto);

        List<GetInvoiceDTO> result = invoiceService.getAllInvoices();

        assertEquals(1, result.size());
        assertEquals("INV-20260414-ABCDE", result.get(0).id());
    }
}
