package com.smartRestaurant.inventory.dto.Addition;

import java.util.List;

public record GetAdditionDetailDTO(
        String id,
        String name,
        String description,
        List<String> photos,
        String additionType,

        /**
         * Precio de compra. Solo presente para adiciones SIMPLE.
         */
        Double purchasePrice,

        double salePrice,

        /**
         * Costo estimado calculado de la receta. Solo para PREPARED.
         */
        Double estimatedCost,

        /**
         * Margen = salePrice - estimatedCost. Solo para PREPARED.
         */
        Double margin,

        int units,
        int reservedUnits,
        int availableUnits,
        int minimumStock,
        String state,

        /**
         * Receta de ingredientes. Solo para PREPARED.
         */
        List<GetAdditionRecipeDTO> recipes
) {
}
