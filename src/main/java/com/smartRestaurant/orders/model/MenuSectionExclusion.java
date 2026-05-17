package com.smartRestaurant.orders.model;

import com.smartRestaurant.inventory.model.PublicationSection;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSectionExclusion {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private MenuOrderInstance instance;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PublicationSection section;
}
