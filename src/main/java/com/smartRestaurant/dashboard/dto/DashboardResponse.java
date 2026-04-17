package com.smartRestaurant.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    /** Ingresos del día, semana y mes (facturas pagadas) */
    private RevenueMetricDTO revenue;

    /** Órdenes en estado PENDING o IN_PROGRESS ahora mismo */
    private long activeOrders;

    /** Promedio del total de facturas pagadas en los últimos 30 días */
    private double averageTicket;

    /** Top 5 platos más vendidos en los últimos 30 días */
    private List<TopDishDTO> topDishes;

    /** Tasa de cancelación de órdenes en los últimos 30 días */
    private CancellationMetricDTO cancellation;

    /** Productos con stock actual por debajo del mínimo configurado */
    private List<LowStockProductDTO> lowStockProducts;

    /** Clientes nuevos vs recurrentes */
    private CustomerMetricDTO customers;
}
