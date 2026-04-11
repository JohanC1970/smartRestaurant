package com.smartRestaurant.orders.service;

import com.smartRestaurant.orders.dto.invoice.CreateInvoiceDTO;
import com.smartRestaurant.orders.dto.invoice.GetInvoiceDTO;
import com.smartRestaurant.orders.dto.payment.PayOnlineDTO;
import com.smartRestaurant.orders.dto.payment.PayPresentialDTO;

import java.util.List;

/**
 * Service para gestionar facturas (invoices)
 */
public interface InvoiceService {
    
    /**
     * Crear factura (se llama automáticamente cuando orden está COMPLETED)
     * SOLO para órdenes ONLINE
     */
    String createInvoice(CreateInvoiceDTO dto);
    
    /**
     * Pagar factura en presencial (mesero registra el pago)
     * Se usa en órdenes PRESENCIAL
     */
    GetInvoiceDTO payPresentialInvoice(PayPresentialDTO dto);
    
    /**
     * Pagar factura online (con Wompi)
     * Se usa en órdenes ONLINE
     */
    GetInvoiceDTO payOnlineInvoice(PayOnlineDTO dto);
    
    /**
     * Obtener detalle de factura
     */
    GetInvoiceDTO getInvoice(String invoiceId);
    
    /**
     * Obtener todas las facturas
     */
    List<GetInvoiceDTO> getAllInvoices();
    
    /**
     * Cancelar factura (solo si no está pagada)
     */
    void cancelInvoice(String invoiceId);
}

