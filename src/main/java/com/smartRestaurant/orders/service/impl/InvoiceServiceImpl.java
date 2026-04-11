package com.smartRestaurant.orders.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.orders.dto.invoice.CreateInvoiceDTO;
import com.smartRestaurant.orders.dto.invoice.GetInvoiceDTO;
import com.smartRestaurant.orders.dto.payment.PayOnlineDTO;
import com.smartRestaurant.orders.dto.payment.PayPresentialDTO;
import com.smartRestaurant.orders.mapper.InvoiceMapper;
import com.smartRestaurant.orders.mapper.PaymentMapper;
import com.smartRestaurant.orders.model.Invoice;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.Payment;
import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import com.smartRestaurant.orders.model.enums.OrderChannel;
import com.smartRestaurant.orders.model.enums.OrderPaymentStatus;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.orders.model.enums.PaymentMethodType;
import com.smartRestaurant.orders.model.enums.PaymentStatus;
import com.smartRestaurant.orders.repository.InvoiceRepository;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.PaymentRepository;
import com.smartRestaurant.orders.service.InvoiceService;
import com.smartRestaurant.orders.service.WompiPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de facturas
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InvoiceServiceImpl implements InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;
    private final WompiPaymentClient wompiClient;
    
    @Override
    public String createInvoice(CreateInvoiceDTO dto) {
        log.info(" [INVOICE] Creando factura para orden: {}", dto.orderId());
        
        // 1. Validar que la orden existe
        Order order = orderRepository.findById(dto.orderId())
            .orElseThrow(() -> {
                log.error(" [INVOICE] Orden no encontrada: {}", dto.orderId());
                return new ResourceNotFoundException("Orden no encontrada");
            });
        
        // 2. Validar que sea orden ONLINE
      //  if (!order.getChannel().equals(OrderChannel.ONLINE)) {
       //     log.warn(" [INVOICE] Intento de crear factura en orden PRESENCIAL: {}", dto.orderId());
         //   throw new BadRequestException("Solo órdenes ONLINE generan factura automática");
       // }
        
        // 3. Usar MAPPER para convertir DTO a Entity
        Invoice invoice = invoiceMapper.toEntity(dto);
        invoice.setOrder(order);
        
        // 4. Guardar en BD
        Invoice saved = invoiceRepository.save(invoice);
        
        // 5. Actualizar orden con estado de pago
        order.setPaymentStatus(OrderPaymentStatus.PENDING);
        orderRepository.save(order);
        
        log.info(" [INVOICE] Factura creada: {} | Total: {} COP", saved.getId(), saved.getTotal());
        return saved.getId();
    }
    
    @Override
    public GetInvoiceDTO payPresentialInvoice(PayPresentialDTO dto) {
        log.info("💰 [INVOICE-PRESENCIAL] Registrando pago para factura: {}", dto.invoiceId());
        
        // 1. Obtener factura
        Invoice invoice = invoiceRepository.findById(dto.invoiceId())
            .orElseThrow(() -> {
                log.error(" [INVOICE-PRESENCIAL] Factura no encontrada: {}", dto.invoiceId());
                return new ResourceNotFoundException("Factura no encontrada");
            });
        
        // 2. Validar estado
        if (!invoice.getStatus().equals(InvoiceStatus.PENDING)) {
            log.warn(" [INVOICE-PRESENCIAL] Factura no está pendiente: {}", dto.invoiceId());
            throw new BadRequestException("Factura no está pendiente de pago");
        }
        
        // 3. Crear Payment usando MAPPER
        Payment payment = paymentMapper.toEntity(dto);
        payment.setInvoice(invoice);
        payment.setOrder(invoice.getOrder());
        payment.setCustomer(invoice.getOrder().getCustomer());
        payment.setAmount(invoice.getTotal());
        payment.setPaymentMethod(dto.paymentMethod());
        payment.setTransactionId(dto.reference());
        payment.setStatus(PaymentStatus.CONFIRMED);
        
        // 4. Guardar Payment
        Payment savedPayment = paymentRepository.save(payment);
        log.info(" [INVOICE-PRESENCIAL] Payment creado: {}", savedPayment.getId());
        
        // 5. Actualizar Invoice
        invoice.setPayment(savedPayment);
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
        
        // 6. Actualizar Orden
        Order order = invoice.getOrder();
        order.setStatus(OrderStatus.DELIVERED);
        order.setPaymentStatus(OrderPaymentStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);
        
        log.info(" [INVOICE-PRESENCIAL] Pago registrado: {} | Método: {} | Total: {} COP",
                 savedPayment.getId(), dto.paymentMethod(), invoice.getTotal());
        
        // 7. Retornar usando MAPPER
        return invoiceMapper.toDTO(invoice);
    }
    
    @Override
    public GetInvoiceDTO payOnlineInvoice(PayOnlineDTO dto) {
        log.info(" [INVOICE-ONLINE] Procesando pago Wompi para factura: {}", dto.invoiceId());
        
        // 1. Obtener factura
        Invoice invoice = invoiceRepository.findById(dto.invoiceId())
            .orElseThrow(() -> {
                log.error(" [INVOICE-ONLINE] Factura no encontrada: {}", dto.invoiceId());
                return new ResourceNotFoundException("Factura no encontrada");
            });
        
        // 2. Validar estado
        if (!invoice.getStatus().equals(InvoiceStatus.PENDING)) {
            log.warn(" [INVOICE-ONLINE] Factura no está pendiente: {}", dto.invoiceId());
            throw new BadRequestException("Factura no está pendiente de pago");
        }
        
        try {
            // 3. Procesar pago con Wompi
            log.info(" [INVOICE-ONLINE] Procesando con Wompi...");
            JsonNode wompiResponse = wompiClient.createTransaction(
                dto.wompiToken(),
                (long)(invoice.getTotal() * 100),  // Convertir a centavos
                invoice.getId(),
                dto.customerEmail(),
                dto.customerPhone(),
                "Pago de factura " + invoice.getId(),
                null
            );
            
            String wompiTransactionId = wompiResponse.path("data").path("id").asText();
            log.info(" [INVOICE-ONLINE] Transacción Wompi: {}", wompiTransactionId);
            
            // 4. Crear Payment usando MAPPER
            Payment payment = paymentMapper.toEntity(dto);
            payment.setInvoice(invoice);
            payment.setOrder(invoice.getOrder());
            payment.setCustomer(invoice.getOrder().getCustomer());
            payment.setAmount(invoice.getTotal());
            payment.setPaymentMethod(PaymentMethodType.WOMPI);
            payment.setTransactionId(wompiTransactionId);
            payment.setStatus(PaymentStatus.CONFIRMED);
            
            // 5. Guardar Payment
            Payment savedPayment = paymentRepository.save(payment);
            log.info(" [INVOICE-ONLINE] Payment creado: {}", savedPayment.getId());
            
            // 6. Actualizar Invoice
            invoice.setPayment(savedPayment);
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            
            // 7. Actualizar Orden (AHORA se envía a cocina)
            Order order = invoice.getOrder();
            order.setStatus(OrderStatus.PENDING);  // Enviar a cocina AHORA
            order.setPaymentStatus(OrderPaymentStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            
            log.info(" [INVOICE-ONLINE] Pago confirmado: {} | Total: {} COP | Orden enviada a cocina",
                     savedPayment.getId(), invoice.getTotal());
            
            // 8. Retornar usando MAPPER
            return invoiceMapper.toDTO(invoice);
            
        } catch (IOException e) {
            log.error(" [INVOICE-ONLINE] Error con Wompi: {}", e.getMessage());
            throw new BadRequestException("Error al procesar pago con Wompi: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public GetInvoiceDTO getInvoice(String invoiceId) {
        log.info("📋 [INVOICE] Obteniendo factura: {}", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> {
                log.error("❌ [INVOICE] Factura no encontrada: {}", invoiceId);
                return new ResourceNotFoundException("Factura no encontrada");
            });
        log.info("✅ [INVOICE] Factura encontrada: {}", invoiceId);
        return invoiceMapper.toDTO(invoice);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GetInvoiceDTO> getAllInvoices() {
        log.info("📋 [INVOICE] Obteniendo todas las facturas");
        List<GetInvoiceDTO> invoices = invoiceRepository.findAll().stream()
            .map(invoiceMapper::toDTO)
            .collect(Collectors.toList());
        log.info("✅ [INVOICE] Se obtuvieron {} facturas", invoices.size());
        return invoices;
    }
    
    @Override
    public void cancelInvoice(String invoiceId) {
        log.info("🚫 [INVOICE] Cancelando factura: {}", invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> {
                log.error("❌ [INVOICE] Factura no encontrada: {}", invoiceId);
                return new ResourceNotFoundException("Factura no encontrada");
            });
        
        if (invoice.getStatus().equals(InvoiceStatus.PAID)) {
            log.warn("⚠️ [INVOICE] Intento de cancelar factura pagada: {}", invoiceId);
            throw new BadRequestException("No se puede cancelar factura pagada");
        }
        
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);
        log.info("✅ [INVOICE] Factura cancelada: {}", invoiceId);
    }
}

