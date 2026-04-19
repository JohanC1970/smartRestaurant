package com.smartRestaurant.restaurant.dto;

import java.time.LocalTime;

public record RestaurantInfoDTO(
        String id,
        String name,
        String description,
        String address,
        String city,
        String phone,
        String email,
        LocalTime openingTime,
        LocalTime closingTime,
        String openDays,
        String logoUrl
) {}
