package com.smartRestaurant.orders.controller;

import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;
import com.smartRestaurant.orders.dto.ConfirmPaymentWithWompiDTO;
import com.smartRestaurant.orders.dto.WompiPaymentResponseDTO;
import com.smartRestaurant.orders.dto.ResponseDTO;
import com.smartRestaurant.orders.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Value("${wompi.api.public-key}")
    private String wompiPublicKey;

    @Value("${wompi.api.integrity-secret}")
    private String wompiIntegritySecret;

    /**
     * POST /api/payments
     * Crear un nuevo pago (métodos tradicionales: EFECTIVO, TARJETA, etc.)
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<String>> createPayment(@RequestBody @Valid CreatePaymentDTO createPaymentDTO) {
        String paymentId = paymentService.createPayment(createPaymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(paymentId, false));
    }

    /**
     * GET /api/payments
     * Obtener todos los pagos registrados
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<List<GetPaymentsDTO>>> getAllPayments() {
        List<GetPaymentsDTO> payments = paymentService.payments();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO<>(payments, false));
    }

    /**
     * GET /api/payments/{id}
     * Obtener detalle de un pago específico
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<GetPaymentDetailDTO>> getPaymentById(@PathVariable String id) {
        GetPaymentDetailDTO payment = paymentService.paymentDetail(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO<>(payment, false));
    }


    // ==================== ENDPOINTS DE WOMPI ====================

    /**
     * POST /api/payments/wompi/confirm
     * Confirmar un pago con Wompi (pasarela de pago colombiana)
     * El cliente envía el token de Wompi generado por Wompi.js
     */
    @PostMapping("/wompi/confirm")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<WompiPaymentResponseDTO>> confirmPaymentWithWompi(
            @RequestBody @Valid ConfirmPaymentWithWompiDTO dto) {
        
        WompiPaymentResponseDTO response = paymentService.confirmPaymentWithWompi(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>(response, false));
    }

    /**
     * POST /api/payments/{id}/wompi/refund
     * Reembolsar un pago de Wompi (solo funciona con pagos de Wompi)
     */
    @PostMapping("/{id}/wompi/refund")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> refundWompiPayment(@PathVariable String id) {
        String result = paymentService.refundWompiPayment(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO<>(result, false));
    }

    /**
     * GET /api/payments/wompi/checkout-params?reference=&amountInCents=
     * Devuelve la llave pública y la firma de integridad para el widget de Wompi.
     * El frontend usa estos datos para renderizar el formulario de pago.
     */
    @GetMapping("/wompi/checkout-params")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<Map<String, String>>> getWompiCheckoutParams(
            @RequestParam String reference,
            @RequestParam long amountInCents,
            @RequestParam(defaultValue = "COP") String currency) {

        try {
            // Firma: SHA256(reference + amountInCents + currency + integritySecret)
            String raw = reference + amountInCents + currency + wompiIntegritySecret;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) { hex.append(String.format("%02x", b)); }

            Map<String, String> params = Map.of(
                "publicKey",  wompiPublicKey,
                "signature",  hex.toString(),
                "reference",  reference,
                "currency",   currency
            );
            return ResponseEntity.ok(new ResponseDTO<>(params, false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO<>(null, true));
        }
    }
}

