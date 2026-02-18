import { Injectable } from '@angular/core';
import { AuthResponse, UserRole } from '../models/auth.models';
import { jwtDecode } from 'jwt-decode';

@Injectable({
    providedIn: 'root'
})
export class TokenService {
    private readonly ACCESS_TOKEN = 'access_token';
    private readonly REFRESH_TOKEN = 'refresh_token';
    private readonly USER_EMAIL = 'user_email';
    private readonly USER_ROLE = 'user_role';

    saveTokens(auth: AuthResponse): void {
        localStorage.setItem(this.ACCESS_TOKEN, auth.accessToken);
        localStorage.setItem(this.REFRESH_TOKEN, auth.refreshToken);
        localStorage.setItem(this.USER_EMAIL, auth.email);
        localStorage.setItem(this.USER_ROLE, auth.role);
    }

    getAccessToken(): string | null {
        return localStorage.getItem(this.ACCESS_TOKEN);
    }

    getRefreshToken(): string | null {
        return localStorage.getItem(this.REFRESH_TOKEN);
    }

    getUserEmail(): string | null {
        return localStorage.getItem(this.USER_EMAIL);
    }

    getUserRole(): UserRole | null {
        return localStorage.getItem(this.USER_ROLE) as UserRole;
    }

    clearTokens(): void {
        localStorage.removeItem(this.ACCESS_TOKEN);
        localStorage.removeItem(this.REFRESH_TOKEN);
        localStorage.removeItem(this.USER_EMAIL);
        localStorage.removeItem(this.USER_ROLE);
    }

    isAuthenticated(): boolean {
        const token = this.getAccessToken();
        if (!token) return false;
        try {
            const decoded: any = jwtDecode(token);
            const isExpired = decoded.exp * 1000 < Date.now();
            return !isExpired;
        } catch (e) {
            return false;
        }
    }
}
