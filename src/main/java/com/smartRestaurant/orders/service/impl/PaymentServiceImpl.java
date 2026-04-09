package com.smartRestaurant.orders.service.impl;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.inventory.exceptions.ResourceNotFoundException;
import com.smartRestaurant.inventory.exceptions.BadRequestException;
import com.smartRestaurant.orders.dto.CreatePaymentDTO;
import com.smartRestaurant.orders.dto.GetPaymentDetailDTO;
import com.smartRestaurant.orders.dto.GetPaymentsDTO;
import com.smartRestaurant.orders.mapper.PaymentMapper;
import com.smartRestaurant.orders.model.Order;
import com.smartRestaurant.orders.model.Payment;
import com.smartRestaurant.orders.repository.OrderRepository;
import com.smartRestaurant.orders.repository.PaymentRepository;
import com.smartRestaurant.orders.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    @Override
    public String createPayment(CreatePaymentDTO createPaymentDTO) {
        log.info("Creando nuevo pago para orden: {}", createPaymentDTO.orderId());

        Order order = orderRepository.findById(createPaymentDTO.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + createPaymentDTO.orderId()));

        if (order.getPayment() != null) {
            throw new BadRequestException("La orden ya tiene un pago asociado");
        }

        User customer = userRepository.findById(createPaymentDTO.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + createPaymentDTO.customerId()));

        Payment payment = paymentMapper.toEntity(createPaymentDTO);
        payment.setOrder(order);
        payment.setCustomer(customer);
        
        Payment savedPayment = paymentRepository.save(payment);
        
        log.info("Pago creado exitosamente: {}", savedPayment.getId());
        return savedPayment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GetPaymentsDTO> payments() {
        log.info("Obteniendo todos los pagos");
        List<Payment> allPayments = paymentRepository.findAll();
        
        if (allPayments.isEmpty()) {
            throw new ResourceNotFoundException("No hay pagos registrados");
        }

        return allPayments.stream()
                .map(paymentMapper::toListDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public GetPaymentDetailDTO paymentDetail(String id) {
        log.info("Obteniendo pago con ID: {}", id);
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado: " + id));
        return paymentMapper.toDetailDTO(payment);
    }
}
