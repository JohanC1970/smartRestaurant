package com.smartRestaurant.auth.service;


import com.smartRestaurant.auth.dto.request.ChangeRoleRequest;
import com.smartRestaurant.auth.dto.request.UpdateUserRequest;
import com.smartRestaurant.auth.dto.response.UserResponse;
import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;

import java.util.List;

public interface AdminService {

    /**
     * Retorna todos los usuarios registrados en el sistema.
     * Opcionalmente filtra por rol y/o estado.
     */
    List<UserResponse> getAllUsers(UserRole role, UserStatus status);

    /**
     * Retorna el detalle de un usuario por su ID.
     */
    UserResponse getUserById(Long id);

    /**
     * Edita el nombre y apellido de un usuario.
     */
    UserResponse updateUser(Long id, UpdateUserRequest request);

    /**
     * Cambia el rol de un usuario.
     * No se puede cambiar el rol del propio admin que realiza la acción.
     */
    UserResponse changeRole(Long id, ChangeRoleRequest request);

    /**
     * Desactiva una cuenta (ACTIVE → INACTIVE).
     */
    UserResponse deactivateUser(Long id);

    /**
     * Reactiva una cuenta (INACTIVE → ACTIVE).
     */
    UserResponse activateUser(Long id);


}
