package com.smartRestaurant.orders.model;

import com.smartRestaurant.inventory.model.MenuPublication;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuOrderInstance {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(nullable = false)
    private MenuPublication publication;

    @Column(length = 100)
    private String seatIdentifier;

    @Column(length = 500)
    private String observation;

    @Builder.Default
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MenuSectionSelection> selections = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "instance", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<MenuSectionExclusion> exclusions = new ArrayList<>();
}
