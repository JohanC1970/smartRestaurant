package com.smartRestaurant.inventory.dto.Dish;

import com.smartRestaurant.inventory.model.Category;
import com.smartRestaurant.inventory.model.State;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GetDishDTO( String id,
                          String name,
                          String description,
                          String price,
                          List<String> photos,
                          Category category) {
}
// aqui no se si devolver todo el objeto de la categoría, creo que mejor devuelvo el nombre o ninguno ya que si entré añ plato ya estaría en la categoría