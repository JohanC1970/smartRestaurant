package com.smartRestaurant.orders.service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;
import com.smartRestaurant.orders.dto.ConfirmPaymentWithWompiDTO;
import com.smartRestaurant.orders.dto.WompiPaymentResponseDTO;
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
import com.smartRestaurant.orders.service.PaymentService;
import com.smartRestaurant.orders.service.SseService;
import com.smartRestaurant.orders.service.WompiPaymentClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentMapper paymentMapper;
    private final WompiPaymentClient wompiPaymentClient;
    private final SseService sseService;

    @Value("${wompi.api.environment:test}")
    private String wompiEnvironment;

    // ==================== MÉTODOS BÁSICOS ====================

    @Override
    public String createPayment(CreatePaymentDTO createPaymentDTO) {
        log.info(" [PAYMENT] Iniciando creación de pago para orden: {}", createPaymentDTO.orderId());
        log.debug(" Detalles: método={}, monto={}, cliente={}",
                  createPaymentDTO.paymentMethod(), 
                  createPaymentDTO.amount(), 
                  createPaymentDTO.customerId());

        // Validar que la orden existe
        Order order = orderRepository.findById(createPaymentDTO.orderId())
                .orElseThrow(() -> {
                    log.error(" Orden no encontrada: {}", createPaymentDTO.orderId());
                    return new ResourceNotFoundException("Orden no encontrada: " + createPaymentDTO.orderId());
                });

        // Validar que la orden no tenga pago asociado
        if (order.getPayment() != null) {
            log.warn(" La orden {} ya tiene un pago asociado", createPaymentDTO.orderId());
            throw new BadRequestException("La orden ya tiene un pago asociado");
        }

        // Validar que el cliente existe
        User customer = userRepository.findById(createPaymentDTO.customerId())
                .orElseThrow(() -> {
                    log.error(" Cliente no encontrado: {}", createPaymentDTO.customerId());
                    return new ResourceNotFoundException("Cliente no encontrado: " + createPaymentDTO.customerId());
                });

        // Crear el pago
        Payment payment = paymentMapper.toEntity(createPaymentDTO);
        payment.setId(UUID.randomUUID().toString());
        payment.setOrder(order);
        payment.setCustomer(customer);
        payment.setCreatedAt(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info(" Pago creado exitosamente: ID={}, monto={}, método={}",
                 savedPayment.getId(), savedPayment.getAmount(), savedPayment.getPaymentMethod());
        return savedPayment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetPaymentsDTO> payments() {
        log.info(" [PAYMENT] Obteniendo lista de todos los pagos");
        List<Payment> allPayments = paymentRepository.findAll();
        
        if (allPayments.isEmpty()) {
            log.warn("⚠ No hay pagos registrados en el sistema");
            throw new ResourceNotFoundException("No hay pagos registrados");
        }

        log.info(" Se obtuvieron {} pagos", allPayments.size());
        return allPayments.stream()
                .map(paymentMapper::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GetPaymentDetailDTO paymentDetail(String id) {
        log.info(" [PAYMENT] Obteniendo detalle del pago: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(" Pago no encontrado: {}", id);
                    return new ResourceNotFoundException("Pago no encontrado: " + id);
                });
        
        log.info(" Pago encontrado: status={}, monto={}", payment.getStatus(), payment.getAmount());
        return paymentMapper.toDetailDTO(payment);
    }


    // ==================== MÉTODOS DE WOMPI ====================

    @Override
    public WompiPaymentResponseDTO confirmPaymentWithWompi(ConfirmPaymentWithWompiDTO dto) {
        log.info("════════════════════════════════════════════════════");
        log.info("[WOMPI-CONFIRM] INICIO — orderId={} customerId={} wompiToken={}",
                dto.orderId(), dto.customerId(), dto.wompiToken());
        log.info("════════════════════════════════════════════════════");

        try {
            // PASO 1 — buscar la orden
            log.info("[WOMPI-CONFIRM] PASO 1: Buscando orden en BD...");
            Order order = orderRepository.findById(dto.orderId())
                    .orElseThrow(() -> {
                        log.error("[WOMPI-CONFIRM] ✗ PASO 1 FALLO: Orden no encontrada en BD: {}", dto.orderId());
                        return new ResourceNotFoundException("Orden no encontrada");
                    });
            log.info("[WOMPI-CONFIRM] ✓ PASO 1 OK: Orden encontrada — status={} paymentStatus={} canal={}",
                    order.getStatus(), order.getPaymentStatus(), order.getChannel());

            // PASO 2 — idempotencia: ¿ya tiene pago?
            log.info("[WOMPI-CONFIRM] PASO 2: Verificando si la orden ya tiene pago...");
            if (order.getPayment() != null) {
                Payment existing = order.getPayment();
                log.warn("[WOMPI-CONFIRM]   Orden ya tiene pago — paymentId={} transactionId={}",
                        existing.getId(), existing.getTransactionId());
                if (dto.wompiToken().equals(existing.getTransactionId())) {
                    log.info("[WOMPI-CONFIRM] ✓ PASO 2 IDEMPOTENTE: mismo transactionId, devolviendo éxito");
                    return new WompiPaymentResponseDTO(
                            existing.getId(), dto.orderId(),
                            existing.getTransactionId(), "APPROVED",
                            (long) (existing.getAmount() * 100), "COP",
                            existing.getPaidAt(), "Pago ya confirmado", "CARD",
                            dto.customerEmail());
                }
                log.error("[WOMPI-CONFIRM] ✗ PASO 2 FALLO: La orden ya tiene un pago con diferente transactionId");
                throw new BadRequestException("La orden ya tiene un pago asociado");
            }
            log.info("[WOMPI-CONFIRM] ✓ PASO 2 OK: La orden no tiene pago previo");

            // PASO 3 — buscar el cliente
            log.info("[WOMPI-CONFIRM] PASO 3: Buscando cliente customerId={}...", dto.customerId());
            User customer = userRepository.findById(dto.customerId())
                    .orElseThrow(() -> {
                        log.error("[WOMPI-CONFIRM] ✗ PASO 3 FALLO: Cliente no encontrado: {}", dto.customerId());
                        return new ResourceNotFoundException("Cliente no encontrado");
                    });
            log.info("[WOMPI-CONFIRM] ✓ PASO 3 OK: Cliente encontrado — email={}", customer.getEmail());

            // PASO 4 — verificar transacción en Wompi
            String wompiTransactionId = dto.wompiToken();
            log.info("[WOMPI-CONFIRM] PASO 4: Consultando Wompi — transactionId={}...", wompiTransactionId);
            JsonNode wompiResponse = wompiPaymentClient.getTransaction(wompiTransactionId);

            String wompiStatus = wompiResponse.path("data").path("status").asText();
            long amountInCents = wompiResponse.path("data").path("amount_in_cents").asLong();
            String wompiRef = wompiResponse.path("data").path("reference").asText();
            String paymentMethod = wompiResponse.path("data").path("payment_method_type").asText();

            log.info("[WOMPI-CONFIRM] ✓ PASO 4 OK: Wompi respondió — status={} monto={} centavos ref={} método={}",
                    wompiStatus, amountInCents, wompiRef, paymentMethod);

            if (!"APPROVED".equalsIgnoreCase(wompiStatus)) {
                log.error("[WOMPI-CONFIRM] ✗ PASO 4 FALLO: Wompi no aprobó — status={}", wompiStatus);
                throw new BadRequestException("El pago no fue aprobado por Wompi. Estado: " + wompiStatus);
            }

            // PASO 5 — crear o reutilizar factura
            double totalAmount = amountInCents / 100.0;
            log.info("[WOMPI-CONFIRM] PASO 5: Gestionando factura — total={} COP...", totalAmount);
            Invoice savedInvoice;

            if (order.getInvoice() != null) {
                savedInvoice = order.getInvoice();
                log.info("[WOMPI-CONFIRM] ✓ PASO 5 OK: Reutilizando factura existente — invoiceId={}", savedInvoice.getId());
            } else {
                double subtotal = Math.round((totalAmount / 1.08) * 100.0) / 100.0;
                double tax      = Math.round((totalAmount - subtotal) * 100.0) / 100.0;
                String invoiceId = "INV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + "-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();

                log.info("[WOMPI-CONFIRM]   Creando factura — id={} subtotal={} tax={} total={}",
                        invoiceId, subtotal, tax, totalAmount);

                Invoice invoice = new Invoice();
                invoice.setId(invoiceId);
                invoice.setOrder(order);
                invoice.setStatus(InvoiceStatus.PAID);
                invoice.setSubtotal(subtotal);
                invoice.setTax(tax);
                invoice.setTotal(totalAmount);
                invoice.setCreatedAt(LocalDateTime.now());
                invoice.setPaidAt(LocalDateTime.now());
                savedInvoice = invoiceRepository.save(invoice);
                log.info("[WOMPI-CONFIRM] ✓ PASO 5 OK: Factura guardada en BD — invoiceId={}", savedInvoice.getId());
            }

            // PASO 6 — guardar pago
            log.info("[WOMPI-CONFIRM] PASO 6: Guardando Payment en BD...");
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrder(order);
            payment.setCustomer(customer);
            payment.setAmount(totalAmount);
            payment.setPaymentMethod(PaymentMethodType.WOMPI);
            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setTransactionId(wompiTransactionId);
            payment.setInvoice(savedInvoice);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setPaidAt(LocalDateTime.now());
            payment.setNotes(dto.notes());

            Payment savedPayment = paymentRepository.save(payment);
            log.info("[WOMPI-CONFIRM] ✓ PASO 6 OK: Payment guardado — paymentId={}", savedPayment.getId());

            // PASO 7 — actualizar orden y notificar cocina
            log.info("[WOMPI-CONFIRM] PASO 7: Actualizando orden y notificando cocina...");
            order.setPaymentStatus(OrderPaymentStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("[WOMPI-CONFIRM]   Orden {} → paymentStatus=CONFIRMED", dto.orderId());
            sseService.notifyKitchen(order);
            log.info("[WOMPI-CONFIRM] ✓ PASO 7 OK: Cocina notificada");

            log.info("════════════════════════════════════════════════════");
            log.info("[WOMPI-CONFIRM] ✓ ÉXITO — paymentId={}", savedPayment.getId());
            log.info("════════════════════════════════════════════════════");

            return new WompiPaymentResponseDTO(
                    savedPayment.getId(), dto.orderId(), wompiTransactionId,
                    wompiStatus, amountInCents, "COP", LocalDateTime.now(),
                    "Pago procesado exitosamente con Wompi", "CARD", dto.customerEmail()
            );

        } catch (IOException e) {
            log.error("════════════════════════════════════════════════════");
            log.error("[WOMPI-CONFIRM] ✗ ERROR DE RED — {}: {}", e.getClass().getSimpleName(), e.getMessage());
            log.error("════════════════════════════════════════════════════");
            throw new BadRequestException("Error al procesar pago con Wompi: " + e.getMessage());
        } catch (BadRequestException | ResourceNotFoundException e) {
            // ya logueados arriba, solo re-lanzar
            throw e;
        } catch (Exception e) {
            log.error("════════════════════════════════════════════════════");
            log.error("[WOMPI-CONFIRM] ✗ ERROR INESPERADO — {}: {}", e.getClass().getName(), e.getMessage(), e);
            log.error("════════════════════════════════════════════════════");
            throw new BadRequestException("Error procesando pago: " + e.getMessage());
        }
    }

    @Override
    public String refundWompiPayment(String paymentId) {
        log.info(" [WOMPI-REFUND] Iniciando reembolso para pago: {}", paymentId);

        try {
            // 1. Obtener el pago
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> {
                        log.error(" [WOMPI-REFUND] Pago no encontrado: {}", paymentId);
                        return new ResourceNotFoundException("Pago no encontrado");
                    });

            // 2. Validar que es un pago de Wompi
            if (!payment.getPaymentMethod().equals(PaymentMethodType.WOMPI)) {
                log.warn(" [WOMPI-REFUND] Intento de reembolsar pago no-Wompi: {}", paymentId);
                throw new BadRequestException("Solo se pueden reembolsar pagos de Wompi");
            }

            // 3. Validar que el pago está confirmado
            if (!payment.getStatus().equals(PaymentStatus.CONFIRMED)) {
                log.warn(" [WOMPI-REFUND] Estado del pago no es CONFIRMED: {}", payment.getStatus());
                throw new BadRequestException("Solo se pueden reembolsar pagos confirmados");
            }

            // 4. Crear reembolso en Wompi
            log.info(" [WOMPI-REFUND] Creando reembolso en Wompi para transacción: {}",
                     payment.getTransactionId());
            
            JsonNode refundResponse = wompiPaymentClient.createRefund(
                    payment.getTransactionId(),
                    (long) (payment.getAmount() * 100) // Convertir de pesos a centavos
            );

            String refundId = refundResponse.path("data").path("id").asText();
            log.info(" [WOMPI-REFUND] Reembolso creado en Wompi: refundId={}", refundId);

            // 5. Actualizar estado del pago
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info(" [WOMPI-REFUND] Pago actualizado en BD: estado=REFUNDED");

            return "Reembolso procesado exitosamente. Refund ID: " + refundId;

        } catch (IOException e) {
            log.error(" [WOMPI-REFUND] Error en Wompi: {}", e.getMessage());
            throw new BadRequestException("Error al procesar reembolso: " + e.getMessage());
        }
    }
}

