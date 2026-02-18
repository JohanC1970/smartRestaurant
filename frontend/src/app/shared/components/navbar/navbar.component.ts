import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { AuthService } from '../../../core/services/auth.service';
import { NavigationService } from '../../../core/services/navigation.service';
import { UserRole } from '../../../models/auth.models';

@Component({
    selector: 'app-navbar',
    standalone: true,
    imports: [CommonModule, MatToolbarModule, MatButtonModule, MatIconModule, MatMenuModule],
    templateUrl: './navbar.component.html',
    styleUrl: './navbar.component.css'
})
export class NavbarComponent implements OnInit {
    userEmail: string = '';
    userRole: UserRole | null = null;
    roleLabel: string = '';

    constructor(
        private authService: AuthService,
        private navigationService: NavigationService
    ) { }

    ngOnInit(): void {
        this.userEmail = this.authService.getCurrentUserEmail() || '';
        this.userRole = this.authService.getCurrentUserRole();
        this.roleLabel = this.navigationService.getRoleLabel(this.userRole);
    }

    logout(): void {
        this.authService.logout();
    }
}
