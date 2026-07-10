import { Component } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink],
  template: `
    <header class="topbar">
      <a routerLink="/" class="brand">
        <span class="brand-icon">⚖️</span>
        <span>
          <span class="brand-title">Sinistre Treatment</span>
          <span class="brand-subtitle">Instruction des dossiers sinistres</span>
        </span>
      </a>
    </header>
    <main class="page">
      <router-outlet></router-outlet>
    </main>
  `,
  styles: [`
    .topbar {
      background: #111827;
      color: #fff;
      padding: 14px 32px;
      display: flex;
      align-items: center;
    }

    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .brand-icon {
      font-size: 26px;
    }

    .brand-title {
      display: block;
      font-weight: 700;
      font-size: 17px;
      letter-spacing: 0.3px;
    }

    .brand-subtitle {
      display: block;
      font-size: 12px;
      color: #9ca3af;
    }

    .page {
      max-width: 1200px;
      margin: 0 auto;
      padding: 32px 24px;
    }
  `]
})
export class App {
}
