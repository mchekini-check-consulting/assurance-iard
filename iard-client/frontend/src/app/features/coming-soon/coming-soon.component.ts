import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-coming-soon',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="coming-soon">
      <div class="content">
        <span class="icon">🚀</span>
        <h1>Bientôt disponible</h1>
        <p>Cette offre d'assurance sera disponible très prochainement.</p>
        <p>Inscrivez-vous pour être informé de son lancement !</p>
        <div class="actions">
          <a routerLink="/" class="btn btn-primary">Retour à l'accueil</a>
          <a routerLink="/register" class="btn btn-outline">Créer un compte</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .coming-soon {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
      padding: 2rem;
    }
    .content {
      text-align: center;
      max-width: 500px;
    }
    .icon {
      font-size: 5rem;
      display: block;
      margin-bottom: 1.5rem;
    }
    h1 {
      font-size: 2rem;
      color: #1e293b;
      margin-bottom: 1rem;
    }
    p {
      color: #64748b;
      margin-bottom: 0.5rem;
      font-size: 1.1rem;
    }
    .actions {
      display: flex;
      gap: 1rem;
      justify-content: center;
      margin-top: 2rem;
      flex-wrap: wrap;
    }
    .btn {
      padding: 0.875rem 1.75rem;
      border-radius: 8px;
      font-weight: 600;
      text-decoration: none;
      transition: all 0.2s;
    }
    .btn-primary {
      background: #2563eb;
      color: white;
    }
    .btn-primary:hover {
      background: #1d4ed8;
    }
    .btn-outline {
      border: 2px solid #2563eb;
      color: #2563eb;
    }
    .btn-outline:hover {
      background: #2563eb;
      color: white;
    }
  `]
})
export class ComingSoonComponent {}
