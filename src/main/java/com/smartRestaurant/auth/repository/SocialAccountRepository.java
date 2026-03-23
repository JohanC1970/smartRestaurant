package com.smartRestaurant.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartRestaurant.auth.model.entity.SocialAccount;
import com.smartRestaurant.auth.model.enums.SocialProvider;

/**
 * Repositorio para gestionar cuentas sociales vinculadas
 */
@Repository
public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    /**
     * Busca una cuenta social por proveedor y ID del proveedor
     * 
     * @param provider   Proveedor social (GOOGLE, FACEBOOK, GITHUB)
     * @param providerId ID único del usuario en el proveedor
     * @return Optional con la cuenta social si existe
     */
    Optional<SocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);

    /**
     * Verifica si existe una cuenta social con el proveedor y ID especificados
     * 
     * @param provider   Proveedor social
     * @param providerId ID del usuario en el proveedor
     * @return true si existe, false en caso contrario
     */
    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
}
