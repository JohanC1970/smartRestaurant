import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="login-container">
      <div class="glass-card">
        <div class="header">
          <div class="logo-placeholder">SR</div>
          <h2>SmartRestaurant</h2>
          <p>Gestión Inteligente de Gastronomía</p>
        </div>
        
        <!-- Login Form -->
        <ng-container *ngIf="mode === 'login'">
          <div class="form-group">
            <label>Email</label>
            <div class="input-wrapper">
              <span class="icon">✉</span>
              <input [(ngModel)]="email" type="email" placeholder="ejemplo@smart.com">
            </div>
          </div>
          
          <div class="form-group">
            <label>Contraseña</label>
            <div class="input-wrapper">
              <span class="icon">🔒</span>
              <input [(ngModel)]="password" type="password" placeholder="••••••••">
            </div>
          </div>
          
          <button (click)="onSubmit()" [disabled]="loading" class="login-btn">
            {{ loading ? 'Iniciando sesión...' : 'Entrar' }}
          </button>
          
          <div class="auth-links">
            <a (click)="mode = 'forgot'">¿Olvidaste tu contraseña?</a>
          </div>
        </ng-container>

        <!-- Forgot Password Form -->
        <ng-container *ngIf="mode === 'forgot'">
          <div class="form-group">
            <label>Ingresa tu correo para recuperar contraseña</label>
            <div class="input-wrapper">
              <span class="icon">✉</span>
              <input [(ngModel)]="email" type="email" placeholder="ejemplo@smart.com">
            </div>
          </div>
          
          <button (click)="onForgotPassword()" [disabled]="loading" class="login-btn">
            {{ loading ? 'Enviando...' : 'Enviar Código' }}
          </button>
          
          <div class="auth-links">
            <a (click)="mode = 'login'">Volver al Login</a>
          </div>
        </ng-container>

        <!-- Reset Password Form -->
        <ng-container *ngIf="mode === 'reset'">
          <div class="form-group">
            <label>Código de Verificación (Email)</label>
            <input [(ngModel)]="otp" placeholder="Código de 6 dígitos">
          </div>
          
          <div class="form-group">
            <label>Nueva Contraseña</label>
            <input [(ngModel)]="newPassword" type="password" placeholder="Mínimo 8 caracteres">
          </div>
          
          <button (click)="onResetPassword()" [disabled]="loading" class="login-btn">
            {{ loading ? 'Restableciendo...' : 'Cambiar Contraseña' }}
          </button>
        </ng-container>

        <p *ngIf="message" [class.error-msg]="!isSuccess" [class.success-msg]="isSuccess">{{ message }}</p>
        
        <div class="footer">
          <p>&copy; 2026 SmartRestaurant System</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 100vh;
      background: linear-gradient(135deg, var(--color1) 0%, var(--color2) 100%);
      padding: 20px;
    }
    .glass-card {
      background: rgba(255, 255, 255, 0.05);
      backdrop-filter: blur(15px);
      border: 1px solid var(--glass-border);
      border-radius: 24px;
      padding: 40px;
      width: 100%;
      max-width: 400px;
      box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
      color: white;
    }
    .header { text-align: center; margin-bottom: 30px; }
    .logo-placeholder {
      width: 60px; height: 60px;
      background: linear-gradient(135deg, var(--color3), var(--color4));
      border-radius: 16px;
      display: flex; align-items: center; justify-content: center;
      font-weight: bold; font-size: 1.5rem; margin: 0 auto 15px;
    }
    h2 { margin: 0; font-size: 1.75rem; }
    p { color: var(--color4); margin: 5px 0 0; font-size: 0.875rem; opacity: 0.8; }
    .form-group { margin-bottom: 20px; }
    label { display: block; font-size: 0.875rem; margin-bottom: 8px; color: var(--color5); opacity: 0.9; }
    .input-wrapper { position: relative; }
    .icon { position: absolute; left: 12px; top: 50%; transform: translateY(-50%); opacity: 0.5; }
    input {
      width: 100%; padding: 12px 12px 12px 40px;
      background: rgba(255, 255, 255, 0.1); border: 1px solid rgba(255, 255, 255, 0.1);
      border-radius: 12px; color: white; outline: none; transition: 0.3s;
    }
    input:focus { border-color: var(--color3); }
    .login-btn {
      width: 100%; padding: 14px; background: var(--color3); color: var(--color1);
      border: none; border-radius: 12px; font-weight: 700; cursor: pointer; transition: 0.3s;
    }
    .login-btn:hover { background: var(--color4); transform: translateY(-2px); }
    .auth-links { text-align: center; margin-top: 15px; }
    .auth-links a { font-size: 0.875rem; color: var(--color4); cursor: pointer; text-decoration: underline; opacity: 0.8; }
    .auth-links a:hover { opacity: 1; }
    .error-msg { color: #ff8a8a; text-align: center; margin-top: 15px; font-size: 0.875rem; }
    .success-msg { color: var(--color4); text-align: center; margin-top: 15px; font-size: 0.875rem; }
    .footer { text-align: center; margin-top: 30px; opacity: 0.4; font-size: 0.75rem; }
  `]
})
export class LoginComponent {
  mode: 'login' | 'forgot' | 'reset' = 'login';
  email = '';
  password = '';
  loading = false;
  message = '';
  isSuccess = false;

  // Reset fields
  otp = '';
  newPassword = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  onSubmit(): void {
    if (!this.email || !this.password) return;
    this.loading = true;
    this.message = '';

    this.authService.login({ email: this.email, password: this.password }).subscribe({
      next: () => {
        this.authService.redirectToDashboard();
        this.loading = false;
      },
      error: (err) => {
        this.message = err.error?.message || 'Error al iniciar sesión';
        this.loading = false;
        this.isSuccess = false;
      }
    });
  }

  onForgotPassword(): void {
    if (!this.email) return;
    this.loading = true;
    this.message = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.isSuccess = true;
        this.message = 'Se ha enviado un código a su email.';
        this.loading = false;
        this.mode = 'reset';
      },
      error: () => {
        this.message = 'Error al enviar código.';
        this.loading = false;
        this.isSuccess = false;
      }
    });
  }

  onResetPassword(): void {
    if (!this.otp || !this.newPassword) return;
    this.loading = true;

    this.authService.resetPassword({
      email: this.email,
      otp: this.otp,
      newPassword: this.newPassword
    }).subscribe({
      next: () => {
        this.isSuccess = true;
        this.message = 'Contraseña actualizada. Inicie sesión.';
        this.loading = false;
        this.mode = 'login';
      },
      error: () => {
        this.message = 'Código inválido o error al actualizar.';
        this.loading = false;
        this.isSuccess = false;
      }
    });
  }
}
