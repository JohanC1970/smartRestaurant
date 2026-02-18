import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-kitchen-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-wrapper">
      <nav class="top-nav">
        <div class="logo">SR <span>Cocina</span></div>
        <div class="user-info">
          <span>Chef de Cocina</span>
          <button (click)="logout()" class="logout-btn">Cerrar Sesión</button>
        </div>
      </nav>
      
      <main class="content">
        <header class="content-header">
          <h1>Dashboard Cocina</h1>
          <p>Gestión de pedidos y tiempos de preparación en tiempo real.</p>
        </header>
        
        <div class="order-board">
          <div class="board-column">
            <h2>Pendientes</h2>
            <div class="order-card">
              <div class="order-header">
                <span class="order-id">#1204</span>
                <span class="time">Hace 5m</span>
              </div>
              <p>2x Pasta Carbonara</p>
              <p>1x Ensalada César</p>
              <button class="status-btn">Iniciar Preparación</button>
            </div>
          </div>
          
          <div class="board-column">
            <h2>En Preparación</h2>
            <div class="order-card active">
              <div class="order-header">
                <span class="order-id">#1201</span>
                <span class="time">Hace 12m</span>
              </div>
              <p>1x Hamburguesa Especial</p>
              <p>1x Papas Fritas</p>
              <button class="status-btn complete">Marcar como Listo</button>
            </div>
          </div>
          
          <div class="board-column">
            <h2>Listos</h2>
            <div class="order-card ready">
              <div class="order-header">
                <span class="order-id">#1198</span>
                <span class="time">Listo</span>
              </div>
              <p>3x Tacos Al Pastor</p>
              <p>2x Refrescos</p>
            </div>
          </div>
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
    .order-board {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 30px;
    }
    .board-column h2 {
      font-size: 1.1rem;
      color: var(--color2);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-bottom: 20px;
      padding-bottom: 10px;
      border-bottom: 2px solid var(--color4);
    }
    .order-card {
      background: white;
      border-radius: 16px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      border: 1px solid rgba(0,0,0,0.05);
    }
    .order-card.active {
      border-left: 4px solid var(--color3);
    }
    .order-card.ready {
      opacity: 0.7;
      background: var(--color5);
    }
    .order-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 15px;
    }
    .order-id {
      font-weight: 800;
      color: var(--color1);
    }
    .time {
      font-size: 0.8rem;
      color: var(--color2);
      background: var(--color4);
      padding: 2px 8px;
      border-radius: 4px;
    }
    .order-card p {
      margin: 5px 0;
      font-size: 0.95rem;
      color: var(--color1);
    }
    .status-btn {
      width: 100%;
      margin-top: 15px;
      padding: 10px;
      border: none;
      border-radius: 8px;
      background: var(--color2);
      color: white;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
    }
    .status-btn:hover {
      background: var(--color1);
    }
    .status-btn.complete {
      background: var(--color3);
      color: var(--color1);
    }
    .status-btn.complete:hover {
      background: var(--color4);
    }
  `]
})
export class KitchenDashboardComponent {
  constructor(private authService: AuthService) { }
  logout() { this.authService.logout(); }
}
