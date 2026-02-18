import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegisterAdminRequest } from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly API_URL = 'http://localhost:8080/admin';

    constructor(private http: HttpClient) { }

    /**
     * Registro de nuevo empleado (Solo Admin)
     */
    registerEmployee(request: RegisterAdminRequest): Observable<string> {
        return this.http.post(`${this.API_URL}/register-employee`, request, {
            responseType: 'text'
        });
    }
}
