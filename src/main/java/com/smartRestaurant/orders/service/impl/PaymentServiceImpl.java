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
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.Payment;
import com.smartRestaurant.orders.model.enums.PaymentMethodType;
import com.smartRestaurant.orders.model.enums.PaymentStatus;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.PaymentRepository;
import com.smartRestaurant.orders.service.PaymentService;
import com.smartRestaurant.orders.service.WompiPaymentClient;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final PaymentMapper paymentMapper;
    private final WompiPaymentClient wompiPaymentClient;

    // ==================== MÉTODOS BÁSICOS ====================

    @Override
    public String createPayment(CreatePaymentDTO createPaymentDTO) {
        log.info("🎯 [PAYMENT] Iniciando creación de pago para orden: {}", createPaymentDTO.orderId());
        log.debug("📊 Detalles: método={}, monto={}, cliente={}", 
                  createPaymentDTO.paymentMethod(), 
                  createPaymentDTO.amount(), 
                  createPaymentDTO.customerId());

        // Validar que la orden existe
        Order order = orderRepository.findById(createPaymentDTO.orderId())
                .orElseThrow(() -> {
                    log.error("❌ Orden no encontrada: {}", createPaymentDTO.orderId());
                    return new ResourceNotFoundException("Orden no encontrada: " + createPaymentDTO.orderId());
                });

        // Validar que la orden no tenga pago asociado
        if (order.getPayment() != null) {
            log.warn("⚠️ La orden {} ya tiene un pago asociado", createPaymentDTO.orderId());
            throw new BadRequestException("La orden ya tiene un pago asociado");
        }

        // Validar que el cliente existe
        User customer = userRepository.findById(createPaymentDTO.customerId())
                .orElseThrow(() -> {
                    log.error("❌ Cliente no encontrado: {}", createPaymentDTO.customerId());
                    return new ResourceNotFoundException("Cliente no encontrado: " + createPaymentDTO.customerId());
                });

        // Crear el pago
        Payment payment = paymentMapper.toEntity(createPaymentDTO);
        payment.setId(UUID.randomUUID().toString());
        payment.setOrder(order);
        payment.setCustomer(customer);
        payment.setCreatedAt(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("✅ Pago creado exitosamente: ID={}, monto={}, método={}", 
                 savedPayment.getId(), savedPayment.getAmount(), savedPayment.getPaymentMethod());
        return savedPayment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetPaymentsDTO> payments() {
        log.info("📋 [PAYMENT] Obteniendo lista de todos los pagos");
        List<Payment> allPayments = paymentRepository.findAll();
        
        if (allPayments.isEmpty()) {
            log.warn("⚠️ No hay pagos registrados en el sistema");
            throw new ResourceNotFoundException("No hay pagos registrados");
        }

        log.info("✅ Se obtuvieron {} pagos", allPayments.size());
        return allPayments.stream()
                .map(paymentMapper::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GetPaymentDetailDTO paymentDetail(String id) {
        log.info("📋 [PAYMENT] Obteniendo detalle del pago: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Pago no encontrado: {}", id);
                    return new ResourceNotFoundException("Pago no encontrado: " + id);
                });
        
        log.info("✅ Pago encontrado: status={}, monto={}", payment.getStatus(), payment.getAmount());
        return paymentMapper.toDetailDTO(payment);
    }


    // ==================== MÉTODOS DE WOMPI ====================

    @Override
    public WompiPaymentResponseDTO confirmPaymentWithWompi(ConfirmPaymentWithWompiDTO dto) {
        log.info("💳 [WOMPI] Iniciando confirmación de pago con Wompi");
        log.info("📊 [WOMPI] Orden: {}, Cliente: {}, Monto: {} centavos COP", 
                 dto.orderId(), dto.customerId(), dto.amount());

        try {
            // 1. Validar que la orden existe
            Order order = orderRepository.findById(dto.orderId())
                    .orElseThrow(() -> {
                        log.error("❌ [WOMPI] Orden no encontrada: {}", dto.orderId());
                        return new ResourceNotFoundException("Orden no encontrada");
                    });

            // 2. Validar que no hay pago anterior
            if (order.getPayment() != null) {
                log.warn("⚠️ [WOMPI] Orden ya tiene pago: {}", dto.orderId());
                throw new BadRequestException("La orden ya tiene un pago asociado");
            }

            // 3. Validar que el cliente existe
            User customer = userRepository.findById(dto.customerId())
                    .orElseThrow(() -> {
                        log.error("❌ [WOMPI] Cliente no encontrado: {}", dto.customerId());
                        return new ResourceNotFoundException("Cliente no encontrado");
                    });

            // 4. Crear transacción en Wompi
            log.info("🔄 [WOMPI] Creando transacción en Wompi...");
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("orderId", dto.orderId());
            metadata.put("customerId", dto.customerId());

            JsonNode wompiResponse = wompiPaymentClient.createTransaction(
                    dto.wompiToken(),
                    dto.amount(),
                    dto.orderId(),
                    dto.customerEmail(),
                    dto.customerPhone(),
                    dto.description(),
                    metadata
            );

            String wompiTransactionId = wompiResponse.path("data").path("id").asText();
            String wompiStatus = wompiResponse.path("data").path("status").asText();
            log.info("✅ [WOMPI] Transacción creada en Wompi: transactionId={}, status={}", 
                     wompiTransactionId, wompiStatus);

            // 5. Guardar pago en la base de datos
            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrder(order);
            payment.setCustomer(customer);
            payment.setAmount(dto.amount() / 100.0); // Convertir de centavos a pesos
            payment.setPaymentMethod(PaymentMethodType.WOMPI);
            payment.setStatus(PaymentStatus.CONFIRMED);
            payment.setTransactionId(wompiTransactionId);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setPaidAt(LocalDateTime.now());
            payment.setNotes(dto.notes());

            Payment savedPayment = paymentRepository.save(payment);
            log.info("✅ [WOMPI] Pago guardado en BD: paymentId={}", savedPayment.getId());

            // 6. Retornar respuesta
            return new WompiPaymentResponseDTO(
                    savedPayment.getId(),
                    dto.orderId(),
                    wompiTransactionId,
                    wompiStatus,
                    dto.amount(),
                    "COP",
                    LocalDateTime.now(),
                    "Pago procesado exitosamente con Wompi",
                    wompiResponse.path("data").path("payment_method").path("type").asText("CARD"),
                    dto.customerEmail()
            );

        } catch (IOException e) {
            log.error("❌ [WOMPI] Error al procesar pago: {}", e.getMessage());
            throw new BadRequestException("Error al procesar pago con Wompi: " + e.getMessage());
        } catch (Exception e) {
            log.error("❌ [WOMPI] Error inesperado: {}", e.getMessage());
            throw new BadRequestException("Error procesando pago: " + e.getMessage());
        }
    }

    @Override
    public String refundWompiPayment(String paymentId) {
        log.info("💰 [WOMPI-REFUND] Iniciando reembolso para pago: {}", paymentId);

        try {
            // 1. Obtener el pago
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> {
                        log.error("❌ [WOMPI-REFUND] Pago no encontrado: {}", paymentId);
                        return new ResourceNotFoundException("Pago no encontrado");
                    });

            // 2. Validar que es un pago de Wompi
            if (!payment.getPaymentMethod().equals("WOMPI")) {
                log.warn("⚠️ [WOMPI-REFUND] Intento de reembolsar pago no-Wompi: {}", paymentId);
                throw new BadRequestException("Solo se pueden reembolsar pagos de Wompi");
            }

            // 3. Validar que el pago está confirmado
            if (!payment.getStatus().equals(PaymentStatus.CONFIRMED)) {
                log.warn("⚠️ [WOMPI-REFUND] Estado del pago no es CONFIRMED: {}", payment.getStatus());
                throw new BadRequestException("Solo se pueden reembolsar pagos confirmados");
            }

            // 4. Crear reembolso en Wompi
            log.info("🔄 [WOMPI-REFUND] Creando reembolso en Wompi para transacción: {}", 
                     payment.getTransactionId());
            
            JsonNode refundResponse = wompiPaymentClient.createRefund(
                    payment.getTransactionId(),
                    (long) (payment.getAmount() * 100) // Convertir de pesos a centavos
            );

            String refundId = refundResponse.path("data").path("id").asText();
            log.info("✅ [WOMPI-REFUND] Reembolso creado en Wompi: refundId={}", refundId);

            // 5. Actualizar estado del pago
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            log.info("✅ [WOMPI-REFUND] Pago actualizado en BD: estado=REFUNDED");

            return "Reembolso procesado exitosamente. Refund ID: " + refundId;

        } catch (IOException e) {
            log.error("❌ [WOMPI-REFUND] Error en Wompi: {}", e.getMessage());
            throw new BadRequestException("Error al procesar reembolso: " + e.getMessage());
        }
    }
}

