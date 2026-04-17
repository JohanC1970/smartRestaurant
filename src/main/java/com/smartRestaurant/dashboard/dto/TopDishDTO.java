package com.smartRestaurant.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopDishDTO {
    private String id;
    private String name;
    private double price;
    private long totalSold;
    private double totalRevenue;
}
