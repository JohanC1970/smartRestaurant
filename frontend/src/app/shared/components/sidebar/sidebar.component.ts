import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../../core/services/auth.service';
import { NavigationService, MenuItem } from '../../../core/services/navigation.service';

@Component({
    selector: 'app-sidebar',
    standalone: true,
    imports: [CommonModule, RouterLink, RouterLinkActive, MatListModule, MatIconModule],
    templateUrl: './sidebar.component.html',
    styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
    menuItems: MenuItem[] = [];

    constructor(
        private authService: AuthService,
        private navigationService: NavigationService
    ) { }

    ngOnInit(): void {
        const role = this.authService.getCurrentUserRole();
        this.menuItems = this.navigationService.getMenuItemsByRole(role);
    }
}
