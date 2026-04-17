package com.smartRestaurant.restaurant.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "restaurant_info")
@Getter
@Setter
public class RestaurantInfo {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 300)
    private String description;

    @Column(length = 200)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    private LocalTime openingTime;

    @Column(nullable = false)
    private LocalTime closingTime;

    @Column(length = 200)
    private String openDays; // e.g. "Lunes - Domingo"

    @Column(length = 500)
    private String logoUrl;
}
