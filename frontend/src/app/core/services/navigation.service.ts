import { Injectable } from '@angular/core';
import { UserRole } from '../../models/auth.models';

export interface MenuItem {
    label: string;
    icon: string;
    route: string;
}

@Injectable({
    providedIn: 'root'
})
export class NavigationService {

    constructor() { }

    /**
     * Obtiene los elementos del menú según el rol del usuario
     */
    getMenuItemsByRole(role: UserRole | null): MenuItem[] {
        if (!role) return [];

        switch (role) {
            case UserRole.ADMIN:
                return [
                    { label: 'Dashboard', icon: 'dashboard', route: '/admin' },
                    { label: 'Usuarios', icon: 'people', route: '/admin/users' },
                    { label: 'Platos', icon: 'restaurant_menu', route: '/admin/menu' },
                    { label: 'Reportes', icon: 'assessment', route: '/admin/reports' },
                    { label: 'Configuración', icon: 'settings', route: '/admin/settings' }
                ];
            case UserRole.KITCHEN:
                return [
                    { label: 'Dashboard', icon: 'dashboard', route: '/kitchen' },
                    { label: 'Ordenes', icon: 'receipt_long', route: '/kitchen/orders' },
                    { label: 'Inventario', icon: 'inventory_2', route: '/kitchen/inventory' },
                    { label: 'Perfil', icon: 'person', route: '/profile' }
                ];
            case UserRole.WAITER:
                return [
                    { label: 'Dashboard', icon: 'dashboard', route: '/waiter' },
                    { label: 'Mesas', icon: 'grid_view', route: '/waiter/tables' },
                    { label: 'Nueva Orden', icon: 'add_shopping_cart', route: '/waiter/orders/new' },
                    { label: 'Perfil', icon: 'person', route: '/profile' }
                ];
            case UserRole.CUSTOMER:
                return [
                    { label: 'Dashboard', icon: 'dashboard', route: '/customer' },
                    { label: 'Menú', icon: 'restaurant', route: '/customer/menu' },
                    { label: 'Mis Pedidos', icon: 'history', route: '/customer/orders' },
                    { label: 'Perfil', icon: 'person', route: '/profile' }
                ];
            default:
                return [];
        }
    }

    /**
     * Obtiene la etiqueta amigable para un rol
     */
    getRoleLabel(role: UserRole | null): string {
        switch (role) {
            case UserRole.ADMIN: return 'Administrador';
            case UserRole.KITCHEN: return 'Cocina';
            case UserRole.WAITER: return 'Mesero';
            case UserRole.CUSTOMER: return 'Cliente';
            default: return '';
        }
    }
}
