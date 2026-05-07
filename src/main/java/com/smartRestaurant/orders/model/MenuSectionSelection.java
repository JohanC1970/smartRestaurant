package com.smartRestaurant.orders.model;

import com.smartRestaurant.inventory.model.PublicationSection;
import com.smartRestaurant.inventory.model.SectionOption;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSectionSelection {

    @Id
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private MenuOrderInstance instance;

    @ManyToOne
    @JoinColumn(nullable = false)
    private PublicationSection section;

    @ManyToOne
    @JoinColumn(nullable = false)
    private SectionOption selectedOption;

    @Column(length = 500)
    private String observation;
}
