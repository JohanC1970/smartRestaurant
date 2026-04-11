package com.smartRestaurant.orders.service;

import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;
import com.smartRestaurant.orders.dto.ConfirmPaymentWithWompiDTO;
import com.smartRestaurant.orders.dto.WompiPaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    String createPayment(CreatePaymentDTO createPaymentDTO);
    List<GetPaymentsDTO> payments();
    GetPaymentDetailDTO paymentDetail(String id);
    

    /**
     * Confirmar y procesar un pago con Wompi
     * Crea una transacción en Wompi y guarda el pago en la base de datos
     * @param dto DTO con datos de Wompi
     * @return Respuesta con detalles del pago procesado
     */
    WompiPaymentResponseDTO confirmPaymentWithWompi(ConfirmPaymentWithWompiDTO dto);

    /**
     * Refundar un pago de Wompi (reembolso total)
     * @param paymentId ID del pago a reembolsar
     * @return Mensaje de confirmación
     */
    String refundWompiPayment(String paymentId);
}


