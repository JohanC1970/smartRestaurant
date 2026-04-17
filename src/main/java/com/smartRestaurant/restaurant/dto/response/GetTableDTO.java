package com.smartRestaurant.restaurant.dto.response;

import com.smartRestaurant.restaurant.model.enums.TableStatus;

public record GetTableDTO(
        String id,
        int number,
        int capacity,
        TableStatus status,
        String location,
        boolean active
) {}
