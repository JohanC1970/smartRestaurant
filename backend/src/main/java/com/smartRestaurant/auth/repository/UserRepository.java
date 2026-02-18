package com.smartRestaurant.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su email
     * 
     * @param email Email del usuario
     * @return Optional con el usuario si existe
     */
    Optional<User> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email especificado
     * 
     * @param email Email a verificar
     * @return true si existe un usuario con ese email
     */
    boolean existsByEmail(String email);

    /**
     * Busca usuarios por rol
     * 
     * @param role Rol a buscar
     * @return Lista de usuarios con ese rol
     */
    List<User> findByRole(UserRole role);

    /**
     * Busca usuarios por estado
     * 
     * @param status Estado a buscar
     * @return Lista de usuarios con ese estado
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Busca usuarios por rol y estado
     * 
     * @param role   Rol a buscar
     * @param status Estado a buscar
     * @return Lista de usuarios que coinciden
     */
    List<User> findByRoleAndStatus(UserRole role, UserStatus status);

    /**
     * Busca usuarios activos (estado ACTIVE)
     * 
     * @return Lista de usuarios activos
     */
    @Query("SELECT u FROM User u WHERE u.status = 'ACTIVE'")
    List<User> findActiveUsers();

    /**
     * Busca usuarios pendientes de verificación
     * 
     * @return Lista de usuarios pendientes
     */
    @Query("SELECT u FROM User u WHERE u.status = 'PENDING' AND u.isEmailVerified = false")
    List<User> findPendingVerification();

    /**
     * Busca usuarios bloqueados (BANNED o con lockedAt no nulo)
     * 
     * @return Lista de usuarios bloqueados
     */
    @Query("SELECT u FROM User u WHERE u.status = 'BANNED' OR u.lockedAt IS NOT NULL")
    List<User> findLockedUsers();

    /**
     * Busca usuarios por nombre o apellido (búsqueda parcial, case-insensitive)
     * 
     * @param searchTerm Término de búsqueda
     * @return Lista de usuarios que coinciden
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchByName(@Param("searchTerm") String searchTerm);

    /**
     * Busca usuarios del staff (ADMIN, KITCHEN, WAITER)
     * 
     * @return Lista de usuarios del staff
     */
    @Query("SELECT u FROM User u WHERE u.role IN ('ADMIN', 'KITCHEN', 'WAITER')")
    List<User> findStaffUsers();

    /**
     * Cuenta usuarios por rol
     * 
     * @param role Rol a contar
     * @return Número de usuarios con ese rol
     */
    long countByRole(UserRole role);

    /**
     * Cuenta usuarios por estado
     * 
     * @param status Estado a contar
     * @return Número de usuarios con ese estado
     */
    long countByStatus(UserStatus status);
}
