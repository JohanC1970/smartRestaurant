package com.smartRestaurant.inventory.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Notification {

    @Id
    private String id;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 250)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
