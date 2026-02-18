import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { AdminService } from '../../../core/services/admin.service';
import { UserRole, RegisterAdminRequest } from '../../../core/models/auth.models';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="dashboard-wrapper">
      <nav class="top-nav">
        <div class="logo">SR <span>Admin</span></div>
        <div class="user-info">
          <span>Administrador</span>
          <button (click)="logout()" class="logout-btn">Cerrar Sesión</button>
        </div>
      </nav>
      
      <main class="content">
        <header class="content-header">
          <h1>Dashboard Administrador</h1>
          <p>Bienvenido al panel de control central de SmartRestaurant.</p>
        </header>

        <section class="admin-section">
          <div class="card">
            <div class="card-header">
              <h2>Crear Nuevo Usuario</h2>
              <p>Registre personal de cocina, meseros u otros administradores.</p>
            </div>
            
            <form (ngSubmit)="onCreateUser()" class="user-form">
              <div class="form-row">
                <div class="form-group">
                  <label>Nombre</label>
                  <input [(ngModel)]="newUser.firstName" name="firstName" placeholder="Nombre" required>
                </div>
                <div class="form-group">
                  <label>Apellido</label>
                  <input [(ngModel)]="newUser.lastName" name="lastName" placeholder="Apellido" required>
                </div>
              </div>

              <div class="form-group">
                <label>Correo Electrónico</label>
                <input [(ngModel)]="newUser.email" name="email" type="email" placeholder="correo@smart.com" required>
              </div>

              <div class="form-group">
                <label>Cargo / Rol</label>
                <select [(ngModel)]="newUser.role" name="role" required>
                  <option [value]="UserRole.KITCHEN">Cocina</option>
                  <option [value]="UserRole.WAITER">Mesero</option>
                  <option [value]="UserRole.ADMIN">Administrador</option>
                </select>
              </div>

              <button type="submit" [disabled]="loading" class="submit-btn">
                {{ loading ? 'Creando...' : 'Crear Usuario' }}
              </button>

              <p *ngIf="message" [class.success]="isSuccess" [class.error]="!isSuccess" class="form-message">
                {{ message }}
              </p>
            </form>
          </div>
        </section>
        
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon">📈</div>
            <div class="stat-data">
              <h3>$12,450</h3>
              <p>Ventas del Día</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">👥</div>
            <div class="stat-data">
              <h3>48</h3>
              <p>Clientes Activos</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">🍽️</div>
            <div class="stat-data">
              <h3>15</h3>
              <p>Órdenes Pendientes</p>
            </div>
          </div>
        </div>
        
        <div class="admin-actions">
          <button class="action-btn">Gestionar Usuarios</button>
          <button class="action-btn">Reportes Mensuales</button>
          <button class="action-btn">Configuración</button>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-wrapper {
      min-height: 100vh;
      background-color: var(--color5);
    }
    .top-nav {
      background-color: var(--color1);
      color: white;
      padding: 0 40px;
      height: 70px;
      display: flex;
      align-items: center;
      justify-content: space-between;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
    }
    .logo {
      font-weight: 800;
      font-size: 1.5rem;
      letter-spacing: -0.05em;
    }
    .logo span {
      color: var(--color3);
      font-size: 0.9rem;
      font-weight: 400;
      margin-left: 10px;
      border-left: 1px solid rgba(255,255,255,0.2);
      padding-left: 10px;
    }
    .user-info {
      display: flex;
      align-items: center;
      gap: 20px;
    }
    .logout-btn {
      background: var(--color2);
      color: white;
      border: none;
      padding: 8px 16px;
      border-radius: 8px;
      cursor: pointer;
      font-weight: 600;
      transition: all 0.2s;
    }
    .logout-btn:hover {
      background: var(--color3);
      transform: translateY(-1px);
    }
    .content {
      padding: 40px;
      max-width: 1200px;
      margin: 0 auto;
    }
    .content-header h1 {
      margin: 0;
      font-size: 2rem;
      color: var(--color1);
    }
    .content-header p {
      color: var(--color2);
      margin: 5px 0 30px;
    }
    .admin-section {
      margin-bottom: 40px;
    }
    .card {
      background: white;
      border-radius: 20px;
      padding: 30px;
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
      border: 1px solid rgba(0,0,0,0.05);
      max-width: 600px;
    }
    .card-header h2 {
      margin: 0;
      color: var(--color1);
      font-size: 1.5rem;
    }
    .card-header p {
      color: var(--color2);
      font-size: 0.875rem;
      margin: 5px 0 25px;
    }
    .form-row {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 15px;
    }
    .form-group {
      margin-bottom: 20px;
    }
    label {
      display: block;
      font-size: 0.875rem;
      font-weight: 600;
      color: var(--color1);
      margin-bottom: 8px;
    }
    input, select {
      width: 100%;
      padding: 12px;
      border: 2px solid var(--color4);
      border-radius: 10px;
      font-size: 0.95rem;
      transition: border-color 0.2s;
    }
    input:focus, select:focus {
      outline: none;
      border-color: var(--color3);
    }
    .submit-btn {
      width: 100%;
      padding: 14px;
      background: var(--color2);
      color: white;
      border: none;
      border-radius: 10px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
    }
    .submit-btn:hover {
      background: var(--color1);
    }
    .submit-btn:disabled {
      opacity: 0.5;
    }
    .form-message {
      margin-top: 15px;
      text-align: center;
      font-size: 0.875rem;
      padding: 10px;
      border-radius: 8px;
    }
    .form-message.success {
      background: rgba(101, 184, 166, 0.1);
      color: #2d6073;
    }
    .form-message.error {
      background: rgba(255, 138, 138, 0.1);
      color: #e53e3e;
    }
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 25px;
      margin-bottom: 40px;
    }
    .stat-card {
      background: white;
      padding: 25px;
      border-radius: 20px;
      display: flex;
      align-items: center;
      gap: 20px;
      box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
      border: 1px solid rgba(0,0,0,0.05);
      transition: transform 0.3s;
    }
    .stat-card:hover {
      transform: translateY(-5px);
    }
    .stat-icon {
      font-size: 2.5rem;
      background: var(--color5);
      width: 60px;
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
    }
    .stat-data h3 {
      margin: 0;
      font-size: 1.5rem;
      color: var(--color1);
    }
    .stat-data p {
      margin: 0;
      color: var(--color2);
      font-size: 0.875rem;
    }
    .admin-actions {
      display: flex;
      gap: 15px;
    }
    .action-btn {
      background: white;
      border: 2px solid var(--color4);
      color: var(--color1);
      padding: 12px 24px;
      border-radius: 12px;
      font-weight: 700;
      cursor: pointer;
      transition: all 0.2s;
    }
    .action-btn:hover {
      background: var(--color4);
      color: var(--color1);
    }
  `]
})
export class AdminDashboardComponent {
  UserRole = UserRole;
  loading = false;
  message = '';
  isSuccess = false;

  newUser: RegisterAdminRequest = {
    firstName: '',
    lastName: '',
    email: '',
    role: UserRole.KITCHEN
  };

  constructor(
    private authService: AuthService,
    private adminService: AdminService
  ) { }

  onCreateUser(): void {
    this.loading = true;
    this.message = '';

    this.adminService.registerEmployee(this.newUser).subscribe({
      next: (resp) => {
        this.isSuccess = true;
        this.message = 'Usuario creado con éxito. Se ha enviado un email con sus credenciales.';
        this.loading = false;
        this.resetForm();
      },
      error: (err) => {
        this.isSuccess = false;
        this.message = 'Error al crear usuario: ' + (err.error || 'Intente nuevamente.');
        this.loading = false;
      }
    });
  }

  private resetForm(): void {
    this.newUser = {
      firstName: '',
      lastName: '',
      email: '',
      role: UserRole.KITCHEN
    };
  }

  logout(): void {
    this.authService.logout();
  }
}
