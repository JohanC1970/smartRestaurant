package com.smartRestaurant.orders.dto;

import com.smartRestaurant.orders.model.enums.PaymentStatus;
import java.time.LocalDateTime;

public record GetPaymentDetailDTO(
        String id,
        String orderId,
        Long customerId,
        PaymentStatus status,
        double amount,
        String paymentMethod,
        LocalDateTime createdAt,
        LocalDateTime paidAt,
        String transactionId,
        String notes
) {}
