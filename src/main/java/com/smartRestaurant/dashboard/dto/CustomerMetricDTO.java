package com.smartRestaurant.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerMetricDTO {
    private long totalCustomers;
    private long newCustomers;       // registrados en los últimos 30 días
    private long returningCustomers; // con más de 1 orden en total
}
