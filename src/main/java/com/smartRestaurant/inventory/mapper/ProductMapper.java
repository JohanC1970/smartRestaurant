package com.smartRestaurant.inventory.mapper;

import com.smartRestaurant.inventory.dto.Product.CreateProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDTO;
import com.smartRestaurant.inventory.dto.Product.GetProductDetailDTO;
import com.smartRestaurant.inventory.dto.Product.UpdateProductDTO;
import com.smartRestaurant.inventory.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = SuplierMapper.class)
public interface ProductMapper {

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID().toString())")
    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "state", constant = "ACTIVE")

    Product toEntity(CreateProductDTO createProductDTO);

    @Mapping(target = "photo", expression = "java(product.getPhotos() != null && !product.getPhotos().isEmpty() ? product.getPhotos().get(0) : null)")
    @Mapping(target = "state", source = "product.state")
    GetProductDTO toDTO(Product product);

    @Mapping(target = "photos", source = "photos")
    @Mapping(target = "state", source = "product.state")
    @Mapping(target = "suplier", source = "product.suplier")
    GetProductDetailDTO toDetailDTO(Product product);

    // Demas atributos que se actualizarán

    void update(UpdateProductDTO updateProductDTO, @MappingTarget Product product);
}
