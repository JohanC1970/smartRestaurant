package com.smartRestaurant.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartRestaurant.auth.model.entity.RefreshToken;
import com.smartRestaurant.auth.model.entity.User;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Busca un refresh token por su valor
     * 
     * @param token Valor del token
     * @return Optional con el token si existe
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Busca un refresh token válido (no expirado) por su valor
     * 
     * @param token Valor del token
     * @return Optional con el token si existe y es válido
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.token = :token " +
            "AND r.fechaExpiracion > :now")
    Optional<RefreshToken> findValidToken(
            @Param("token") String token,
            @Param("now") LocalDateTime now);

    /**
     * Busca todos los refresh tokens de un usuario
     * 
     * @param usuario Usuario
     * @return Lista de tokens del usuario
     */
    List<RefreshToken> findByUsuario(User usuario);

    /**
     * Busca todos los refresh tokens válidos de un usuario
     * 
     * @param usuario Usuario
     * @return Lista de tokens válidos del usuario
     */
    @Query("SELECT r FROM RefreshToken r WHERE r.usuario = :usuario " +
            "AND r.fechaExpiracion > :now")
    List<RefreshToken> findValidTokensByUser(
            @Param("usuario") User usuario,
            @Param("now") LocalDateTime now);

    /**
     * Verifica si existe un token válido para un usuario
     * 
     * @param usuario Usuario
     * @return true si el usuario tiene al menos un token válido
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
            "FROM RefreshToken r WHERE r.usuario = :usuario " +
            "AND r.fechaExpiracion > :now")
    boolean hasValidToken(
            @Param("usuario") User usuario,
            @Param("now") LocalDateTime now);

    /**
     * Elimina todos los refresh tokens de un usuario
     * Útil al hacer logout de todas las sesiones
     * 
     * @param usuario Usuario
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.usuario = :usuario")
    int deleteByUsuario(@Param("usuario") User usuario);

    /**
     * Elimina un refresh token específico por su valor
     * Útil para logout de una sesión específica
     * 
     * @param token Valor del token
     * @return Número de tokens eliminados (0 o 1)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.token = :token")
    int deleteByToken(@Param("token") String token);

    /**
     * Elimina todos los refresh tokens expirados
     * Para limpieza periódica de la base de datos
     * 
     * @param now Fecha actual
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.fechaExpiracion < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Elimina los refresh tokens más antiguos de un usuario, manteniendo solo los N
     * más recientes
     * Útil para limitar el número de sesiones activas por usuario
     * 
     * @param usuario Usuario
     * @param limit   Número máximo de tokens a mantener
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query(value = "DELETE FROM refresh_tokens WHERE id IN " +
            "(SELECT id FROM refresh_tokens WHERE usuario_id = :usuarioId " +
            "ORDER BY fecha_creacion DESC OFFSET :limit)", nativeQuery = true)
    int deleteOldestTokensForUser(
            @Param("usuarioId") Long usuarioId,
            @Param("limit") int limit);

    /**
     * Cuenta el número de refresh tokens válidos de un usuario
     * 
     * @param usuario Usuario
     * @return Número de tokens válidos
     */
    @Query("SELECT COUNT(r) FROM RefreshToken r WHERE r.usuario = :usuario " +
            "AND r.fechaExpiracion > :now")
    long countValidTokensByUser(
            @Param("usuario") User usuario,
            @Param("now") LocalDateTime now);

    /**
     * Cuenta el total de refresh tokens de un usuario (válidos y expirados)
     * 
     * @param usuario Usuario
     * @return Número total de tokens
     */
    long countByUsuario(User usuario);
}
