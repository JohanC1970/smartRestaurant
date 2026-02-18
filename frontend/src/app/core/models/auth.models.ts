export enum UserRole {
    ADMIN = 'ADMIN',
    KITCHEN = 'KITCHEN',
    WAITER = 'WAITER',
    CUSTOMER = 'CUSTOMER'
}

export interface AuthResponse {
    accessToken: string;
    refreshToken: string;
    email: string;
    role: UserRole;
}

export interface LoginRequest {
    email: string;
    password?: string;
}

export interface RegisterRequest {
    email: string;
    password?: string;
    firstName: string;
    lastName: string;
    role: UserRole;
}

export interface RegisterAdminRequest {
    firstName: string;
    lastName: string;
    email: string;
    role: UserRole;
}

export interface VerifyRequest {
    email: string;
    code: string;
}

export interface ResetPasswordRequest {
    email: string;
    otp: string;
    newPassword: string;
}
