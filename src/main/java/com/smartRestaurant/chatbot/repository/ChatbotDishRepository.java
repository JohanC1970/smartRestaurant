package com.smartRestaurant.chatbot.repository;

import com.smartRestaurant.inventory.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para las consultas del chatbot sobre platos.
 *
 * <p>Extiende {@link JpaRepository} para operaciones CRUD estándar y define
 * queries personalizadas optimizadas para los casos de uso del chatbot.</p>
 */
@Repository
public interface ChatbotDishRepository extends JpaRepository<Dish, String> {

    /**
     * Busca platos activos filtrando opcionalmente por texto libre y precio máximo.
     *
     * <p>El filtro de texto busca coincidencias parciales (case-insensitive) tanto
     * en el nombre como en la descripción del plato. Ambos parámetros son opcionales:
     * si son {@code null}, ese filtro se ignora.</p>
     *
     * @param q        término de búsqueda; si es {@code null} no se aplica filtro de texto
     * @param maxPrice precio máximo; si es {@code null} no se aplica filtro de precio
     * @return lista de platos activos que cumplen los criterios
     */
    @Query("SELECT d FROM Dish d WHERE d.state = 'ACTIVE' " +
            "AND (:q IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(d.description) LIKE LOWER(CONCAT('%', :q, '%'))) " +
            "AND (:maxPrice IS NULL OR d.price <= :maxPrice)")
    List<Dish> searchForChatbot(@Param("q") String q, @Param("maxPrice") Double maxPrice);

    /**
     * Retorna todos los platos activos que pertenecen a una categoría específica.
     *
     * <p>La comparación del nombre de categoría es insensible a mayúsculas/minúsculas.</p>
     *
     * @param categoryName nombre de la categoría a filtrar
     * @return lista de platos activos de la categoría indicada
     */
    @Query("SELECT d FROM Dish d WHERE d.state = 'ACTIVE' AND LOWER(d.category.name) = LOWER(:categoryName)")
    List<Dish> findActiveByCategoryName(@Param("categoryName") String categoryName);
}
