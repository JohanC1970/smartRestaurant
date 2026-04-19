package com.smartRestaurant.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record UpdateRestaurantInfoDTO(
        @NotBlank String name,
        String description,
        String address,
        String city,
        String phone,
        @Email String email,
        @NotNull LocalTime openingTime,
        @NotNull LocalTime closingTime,
        String openDays,
        String logoUrl
) {}
