package com.smartRestaurant.inventory.dto.Addition;

import jakarta.persistence.Column;
import jakarta.persistence.Id;

public record GetAdditionDTO(String id,
                             String name,
                             String photo,
                             double price) {

}
