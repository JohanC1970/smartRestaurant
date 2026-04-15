package com.smartRestaurant.orders.mapper;

import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;
import com.smartRestaurant.orders.dto.payment.PayOnlineDTO;
import com.smartRestaurant.orders.dto.payment.PayPresentialDTO;
import com.smartRestaurant.orders.model.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PaymentMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "PAID")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paidAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    Payment toEntity(CreatePaymentDTO createPaymentDTO);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "customer.id", target = "customerId")
    GetPaymentDetailDTO toDetailDTO(Payment payment);

    @Mapping(source = "order.id", target = "orderId")
    @Mapping(source = "customer.id", target = "customerId")
    GetPaymentsDTO toListDTO(Payment payment);

    /**
     * Convertir PayPresentialDTO a Payment entity (pago presencial)
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "CONFIRMED")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paidAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    Payment toEntity(PayPresentialDTO dto);

    /**
     * Convertir PayOnlineDTO a Payment entity (pago online Wompi)
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "status", constant = "CONFIRMED")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paidAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    Payment toEntity(PayOnlineDTO dto);
}


