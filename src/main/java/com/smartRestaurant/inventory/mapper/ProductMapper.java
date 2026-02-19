package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ProductMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")

    Product toEntity(CreateProductDTO createProductDTO);
    GetProductDTO toDTO(Product product);

    // Demas atributos que se actualizarán

    void update(UpdateProductDTO updateProductDTO, @MappingTarget Product product);
}
