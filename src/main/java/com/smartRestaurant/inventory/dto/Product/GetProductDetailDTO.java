package com.smartRestaurant.inventory.dto.Product;

import com.smartRestaurant.inventory.dto.Suplier.GetSuplierDTO;

import java.util.List;

public record GetProductDetailDTO(String id,
                                  String name,
                                  String description,
                                  double price,
                                  double weight,
                                  List<String> photos,
                                  double minimumStock,
                                  GetSuplierDTO suplier) {
}
