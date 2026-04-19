package com.smartRestaurant.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LowStockProductDTO {
    private String id;
    private String name;
    private double minimumStock;
    private double currentStock;
}
