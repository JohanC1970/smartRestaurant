package com.smartRestaurant.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationMetricDTO {
    private long total;
    private long cancelled;
    private double rate; // porcentaje redondeado a 2 decimales
}
