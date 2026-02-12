package com.smartRestaurant.auth.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartRestaurant.auth.model.entity.OtpToken;
import com.smartRestaurant.auth.model.entity.User;
import com.smartRestaurant.auth.model.enums.OtpTokenType;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    /**
     * Busca un token OTP válido (no usado y no expirado) por código y tipo
     * 
     * @param codigoOtp Código OTP
     * @param tipo      Tipo de token
     * @return Optional con el token si existe y es válido
     */
    @Query("SELECT o FROM OtpToken o WHERE o.codigoOtp = :codigo " +
            "AND o.tipo = :tipo " +
            "AND o.usado = false " +
            "AND o.fechaExpiracion > :now")
    Optional<OtpToken> findValidToken(
            @Param("codigo") String codigoOtp,
            @Param("tipo") OtpTokenType tipo,
            @Param("now") LocalDateTime now);

    /**
     * Busca un token OTP válido por usuario, código y tipo
     * 
     * @param usuario   Usuario propietario del token
     * @param codigoOtp Código OTP
     * @param tipo      Tipo de token
     * @return Optional con el token si existe y es válido
     */
    @Query("SELECT o FROM OtpToken o WHERE o.usuario = :usuario " +
            "AND o.codigoOtp = :codigo " +
            "AND o.tipo = :tipo " +
            "AND o.usado = false " +
            "AND o.fechaExpiracion > :now")
    Optional<OtpToken> findValidTokenByUser(
            @Param("usuario") User usuario,
            @Param("codigo") String codigoOtp,
            @Param("tipo") OtpTokenType tipo,
            @Param("now") LocalDateTime now);

    /**
     * Busca todos los tokens de un usuario por tipo
     * 
     * @param usuario Usuario
     * @param tipo    Tipo de token
     * @return Lista de tokens
     */
    List<OtpToken> findByUsuarioAndTipo(User usuario, OtpTokenType tipo);

    /**
     * Busca todos los tokens válidos de un usuario por tipo
     * 
     * @param usuario Usuario
     * @param tipo    Tipo de token
     * @return Lista de tokens válidos
     */
    @Query("SELECT o FROM OtpToken o WHERE o.usuario = :usuario " +
            "AND o.tipo = :tipo " +
            "AND o.usado = false " +
            "AND o.fechaExpiracion > :now")
    List<OtpToken> findValidTokensByUserAndType(
            @Param("usuario") User usuario,
            @Param("tipo") OtpTokenType tipo,
            @Param("now") LocalDateTime now);

    /**
     * Marca como usados todos los tokens válidos de un usuario por tipo
     * Útil para invalidar tokens anteriores al generar uno nuevo
     * 
     * @param usuario Usuario
     * @param tipo    Tipo de token
     */
    @Modifying
    @Query("UPDATE OtpToken o SET o.usado = true " +
            "WHERE o.usuario = :usuario " +
            "AND o.tipo = :tipo " +
            "AND o.usado = false " +
            "AND o.fechaExpiracion > :now")
    void invalidateUserTokensByType(
            @Param("usuario") User usuario,
            @Param("tipo") OtpTokenType tipo,
            @Param("now") LocalDateTime now);

    /**
     * Elimina tokens expirados (limpieza periódica)
     * 
     * @param now Fecha actual
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.fechaExpiracion < :now")
    int deleteExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Elimina tokens usados más antiguos que la fecha especificada
     * 
     * @param date Fecha límite
     * @return Número de tokens eliminados
     */
    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.usado = true AND o.fechaCreacion < :date")
    int deleteOldUsedTokens(@Param("date") LocalDateTime date);

    /**
     * Cuenta tokens válidos de un usuario por tipo
     * 
     * @param usuario Usuario
     * @param tipo    Tipo de token
     * @return Número de tokens válidos
     */
    @Query("SELECT COUNT(o) FROM OtpToken o WHERE o.usuario = :usuario " +
            "AND o.tipo = :tipo " +
            "AND o.usado = false " +
            "AND o.fechaExpiracion > :now")
    long countValidTokensByUserAndType(
            @Param("usuario") User usuario,
            @Param("tipo") OtpTokenType tipo,
            @Param("now") LocalDateTime now);
}
