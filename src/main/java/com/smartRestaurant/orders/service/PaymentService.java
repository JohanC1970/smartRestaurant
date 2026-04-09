package com.smartRestaurant.orders.service;

import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;

import java.util.List;

public interface PaymentService {

    String createPayment(CreatePaymentDTO createPaymentDTO);
    List<GetPaymentsDTO> payments();
    GetPaymentDetailDTO paymentDetail(String id);
}
