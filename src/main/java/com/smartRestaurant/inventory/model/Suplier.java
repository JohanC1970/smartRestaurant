package com.smartRestaurant.inventory.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Suplier {

    @Id
    private String id;

    @Column(nullable = false, length = 30)
    private String name;

    @Column(nullable = false, length = 50)
    private String address;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(nullable = false)
    @Email
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private State state;



}
