import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, tap, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import {
    LoginRequest,
    RegisterRequest,
    AuthResponse,
    VerifyRequest,
    ResetPasswordRequest,
    UserRole
} from '../models/auth.models';
import { TokenService } from './token.service';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly API_URL = 'http://localhost:8080/auth';
    private currentUserSubject = new BehaviorSubject<AuthResponse | null>(null);
    public currentUser$ = this.currentUserSubject.asObservable();

    constructor(
        private http: HttpClient,
        private tokenService: TokenService,
        private router: Router
    ) {
        this.loadCurrentUser();
    }

    private loadCurrentUser(): void {
        const token = this.tokenService.getAccessToken();
        const email = this.tokenService.getUserEmail();
        const role = this.tokenService.getUserRole();

        if (token && email && role) {
            this.currentUserSubject.next({
                accessToken: token,
                refreshToken: this.tokenService.getRefreshToken() || '',
                email: email,
                role: role
            });
        }
    }

    /**
     * Login de usuario (REAL API)
     */
    login(request: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.API_URL}/login`, request)
            .pipe(
                tap(response => {
                    this.tokenService.saveTokens(response);
                    this.currentUserSubject.next(response);
                }),
                catchError(error => {
                    console.error('Error en login:', error);
                    return throwError(() => error);
                })
            );
    }

    /**
     * Registro de usuario
     */
    register(request: RegisterRequest): Observable<string> {
        return this.http.post(`${this.API_URL}/register`, request, {
            responseType: 'text'
        });
    }

    /**
     * Logout
     */
    logout(): void {
        this.tokenService.clearTokens();
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
    }

    /**
     * Refrescar token
     */
    refreshToken(): Observable<AuthResponse> {
        const refreshToken = this.tokenService.getRefreshToken();
        return this.http.post<AuthResponse>(`${this.API_URL}/refresh-token`, { refreshToken })
            .pipe(
                tap(response => {
                    this.tokenService.saveTokens(response);
                    this.currentUserSubject.next(response);
                })
            );
    }

    isAuthenticated(): boolean {
        return this.tokenService.isAuthenticated();
    }

    getCurrentUserRole(): UserRole | null {
        return this.tokenService.getUserRole();
    }

    getCurrentUserEmail(): string | null {
        return this.tokenService.getUserEmail();
    }

    redirectToDashboard(): void {
        const role = this.getCurrentUserRole();
        const routes: Record<UserRole, string> = {
            [UserRole.ADMIN]: '/admin',
            [UserRole.KITCHEN]: '/kitchen',
            [UserRole.WAITER]: '/waiter',
            [UserRole.CUSTOMER]: '/customer'
        };

        if (role && routes[role]) {
            this.router.navigate([routes[role]]);
        } else {
            this.router.navigate(['/login']);
        }
    }

    // Otros flujos reales
    forgotPassword(email: string): Observable<string> {
        return this.http.post(`${this.API_URL}/forgot-password`, { email }, { responseType: 'text' });
    }

    resetPassword(request: ResetPasswordRequest): Observable<string> {
        return this.http.post(`${this.API_URL}/reset-password`, request, { responseType: 'text' });
    }

    verify2FA(request: VerifyRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.API_URL}/verify-2fa`, request)
            .pipe(
                tap(response => {
                    this.tokenService.saveTokens(response);
                    this.currentUserSubject.next(response);
                })
            );
    }
}
