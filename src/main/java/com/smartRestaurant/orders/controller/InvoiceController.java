package com.smartRestaurant.orders.controller;

import com.smartRestaurant.orders.dto.invoice.GetInvoiceDTO;
import com.smartRestaurant.orders.dto.payment.PayOnlineDTO;
import com.smartRestaurant.orders.dto.payment.PayPresentialDTO;
import com.smartRestaurant.orders.dto.ResponseDTO;
import com.smartRestaurant.orders.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar facturas (invoices)
 */
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    /**
     * GET /api/invoices/{id}
     * Obtener detalle de factura
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<GetInvoiceDTO>> getInvoice(@PathVariable String id) {
        GetInvoiceDTO invoice = invoiceService.getInvoice(id);
        return ResponseEntity.ok(new ResponseDTO<>(invoice, false));
    }
    
    /**
     * GET /api/invoices
     * Obtener todas las facturas
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<List<GetInvoiceDTO>>> getAllInvoices() {
        List<GetInvoiceDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(new ResponseDTO<>(invoices, false));
    }
    
    /**
     * POST /api/invoices/{id}/pay-presential
     * Pagar factura en presencial (mesero registra el pago)
     * 
     * Body ejemplo:
     * {
     *   "invoiceId": "INV-20260409-XXXXX",
     *   "paymentMethod": "CASH",
     *   "reference": null,
     *   "notes": "Pagó en efectivo"
     * }
     */
    @PostMapping("/{id}/pay-presential")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<GetInvoiceDTO>> payPresential(
            @PathVariable String id,
            @RequestBody @Valid PayPresentialDTO dto) {
        
        GetInvoiceDTO result = invoiceService.payPresentialInvoice(dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO<>(result, false));
    }
    
    /**
     * POST /api/invoices/{id}/pay-online
     * Pagar factura con Wompi (cliente online)
     * 
     * Body ejemplo:
     * {
     *   "invoiceId": "INV-20260409-XXXXX",
     *   "wompiToken": "eJydUsFuwjAM...",
     *   "customerEmail": "cliente@example.com",
     *   "customerPhone": "+573001234567",
     *   "notes": "Pago exitoso"
     * }
     */
    @PostMapping("/{id}/pay-online")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<GetInvoiceDTO>> payOnline(
            @PathVariable String id,
            @RequestBody @Valid PayOnlineDTO dto) {
        
        GetInvoiceDTO result = invoiceService.payOnlineInvoice(dto);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO<>(result, false));
    }
    
    /**
     * DELETE /api/invoices/{id}
     * Cancelar factura (solo si no está pagada)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN')")
    public ResponseEntity<ResponseDTO<String>> cancelInvoice(@PathVariable String id) {
        invoiceService.cancelInvoice(id);
        return ResponseEntity.ok(new ResponseDTO<>("Factura cancelada", false));
    }
}

