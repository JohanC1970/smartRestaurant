package com.smartRestaurant.dashboard.service.impl;

import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.repository.UserRepository;
import com.smartRestaurant.dashboard.dto.*;
import com.smartRestaurant.dashboard.service.DashboardService;
import com.smartRestaurant.inventory.Repository.ProductRepository;
import com.smartRestaurant.orders.model.enums.InvoiceStatus;
import com.smartRestaurant.orders.model.enums.OrderStatus;
import com.smartRestaurant.orders.repository.InvoiceRepository;
import com.smartRestaurant.orders.repository.OrderItemRepository;
import com.smartRestaurant.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderItemRepository orderItemRepository;

    @Override
    public DashboardResponse getDashboard() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime startOfMonth = now.minusDays(30);

        return DashboardResponse.builder()
                .revenue(buildRevenueMetric(now, startOfToday, startOfWeek, startOfMonth))
                .activeOrders(countActiveOrders())
                .averageTicket(getAverageTicket(startOfMonth))
                .topDishes(getTopDishes(startOfMonth))
                .cancellation(getCancellationMetric(startOfMonth))
                .lowStockProducts(getLowStockProducts())
                .customers(getCustomerMetric(startOfMonth))
                .build();
    }

    // ── Métricas privadas ─────────────────────────────────────────────────────

    private RevenueMetricDTO buildRevenueMetric(LocalDateTime now,
                                                LocalDateTime startOfToday,
                                                LocalDateTime startOfWeek,
                                                LocalDateTime startOfMonth) {
        Double todayRevenue = invoiceRepository.sumRevenueByPeriod(InvoiceStatus.PAID, startOfToday, now);
        Double weekRevenue = invoiceRepository.sumRevenueByPeriod(InvoiceStatus.PAID, startOfWeek, now);
        Double monthRevenue = invoiceRepository.sumRevenueByPeriod(InvoiceStatus.PAID, startOfMonth, now);

        return RevenueMetricDTO.builder()
                .today(todayRevenue != null ? todayRevenue : 0.0)
                .thisWeek(weekRevenue != null ? weekRevenue : 0.0)
                .thisMonth(monthRevenue != null ? monthRevenue : 0.0)
                .build();
    }

    private long countActiveOrders() {
        return orderRepository.countByStatusIn(List.of(OrderStatus.PENDING, OrderStatus.IN_PROGRESS));
    }

    private double getAverageTicket(LocalDateTime since) {
        Double avg = invoiceRepository.avgTicketSince(InvoiceStatus.PAID, since);
        return avg != null ? avg : 0.0;
    }

    private List<TopDishDTO> getTopDishes(LocalDateTime since) {
        List<Object[]> rows = orderItemRepository.findTop5DishesSince(since);
        return rows.stream()
                .map(row -> {
                    double price = ((Number) row[2]).doubleValue();
                    long sold = ((Number) row[3]).longValue();
                    return TopDishDTO.builder()
                            .id((String) row[0])
                            .name((String) row[1])
                            .price(price)
                            .totalSold(sold)
                            .totalRevenue(price * sold)
                            .build();
                })
                .toList();
    }

    private CancellationMetricDTO getCancellationMetric(LocalDateTime since) {
        long total = orderRepository.countSince(since);
        long cancelled = orderRepository.countByStatusSince(OrderStatus.CANCELLED, since);
        double rate = total > 0 ? Math.round((cancelled * 100.0 / total) * 100.0) / 100.0 : 0.0;

        return CancellationMetricDTO.builder()
                .total(total)
                .cancelled(cancelled)
                .rate(rate)
                .build();
    }

    private List<LowStockProductDTO> getLowStockProducts() {
        List<Object[]> rows = productRepository.findProductsBelowMinimumStock();
        return rows.stream()
                .map(row -> LowStockProductDTO.builder()
                        .id((String) row[0])
                        .name((String) row[1])
                        .minimumStock(((Number) row[2]).doubleValue())
                        .currentStock(((Number) row[3]).doubleValue())
                        .build())
                .toList();
    }

    private CustomerMetricDTO getCustomerMetric(LocalDateTime since) {
        long total = userRepository.countByRole(UserRole.CUSTOMER);
        long newCustomers = userRepository.countNewByRoleSince(UserRole.CUSTOMER, since);
        long returning = userRepository.countReturningCustomers(UserRole.CUSTOMER);

        return CustomerMetricDTO.builder()
                .totalCustomers(total)
                .newCustomers(newCustomers)
                .returningCustomers(returning)
                .build();
    }
}
