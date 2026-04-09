package com.smartRestaurant.auth.dto.response;


import com.smartRestaurant.auth.model.enums.UserRole;
import com.smartRestaurant.auth.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private UserRole role;
    private String roleDisplayName;
    private UserStatus status;
    private String statusDisplayName;
    private boolean isEmailVerified;
    private boolean requiresPasswordChange;
    private int failedLoginAttempts;
    private String lockReason;
    private LocalDateTime lockedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
