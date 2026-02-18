import { UserRole } from '../models/auth.models';

export interface MockUser {
    email: string;
    password: string;
    role: UserRole;
    firstName: string;
    lastName: string;
}

export const MOCK_USERS: MockUser[] = [
    {
        email: 'admin@smart.com',
        password: 'admin123',
        role: UserRole.ADMIN,
        firstName: 'Admin',
        lastName: 'Sistemas'
    },
    {
        email: 'cocina@smart.com',
        password: 'cocina123',
        role: UserRole.KITCHEN,
        firstName: 'Chef',
        lastName: 'Principal'
    },
    {
        email: 'mesero@smart.com',
        password: 'mesero123',
        role: UserRole.WAITER,
        firstName: 'Mesero',
        lastName: 'Uno'
    },
    {
        email: 'cliente@smart.com',
        password: 'cliente123',
        role: UserRole.CUSTOMER,
        firstName: 'Cliente',
        lastName: 'Prueba'
    }
];
