package com.smartRestaurant.orders.controller;

import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;
import com.smartRestaurant.orders.dto.ResponseDTO;
import com.smartRestaurant.orders.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('order:write', 'ROLE_ADMIN', 'ROLE_WAITER', 'ROLE_CUSTOMER')")
    public ResponseEntity<ResponseDTO<String>> createPayment(@RequestBody @Valid CreatePaymentDTO createPaymentDTO) {
        String paymentId = paymentService.createPayment(createPaymentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseDTO<>(paymentId, false));
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<List<GetPaymentsDTO>>> getAllPayments() {
        List<GetPaymentsDTO> payments = paymentService.payments();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(payments, false));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('order:read', 'ROLE_ADMIN', 'ROLE_WAITER')")
    public ResponseEntity<ResponseDTO<GetPaymentDetailDTO>> getPaymentById(@PathVariable String id) {
        GetPaymentDetailDTO payment = paymentService.paymentDetail(id);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO<>(payment, false));
    }
}
