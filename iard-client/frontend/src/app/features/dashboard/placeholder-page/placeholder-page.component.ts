import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-placeholder-page',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="placeholder-page">
      <div class="placeholder-content">
        <span class="placeholder-icon">{{ icon }}</span>
        <h1>{{ title }}</h1>
        <p>{{ description }}</p>
      </div>
    </div>
  `,
  styles: [`
    .placeholder-page {
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 400px;
    }
    .placeholder-content {
      text-align: center;
      padding: 2rem;
    }
    .placeholder-icon {
      font-size: 4rem;
      display: block;
      margin-bottom: 1rem;
    }
    h1 {
      font-size: 1.5rem;
      color: #1e293b;
      margin-bottom: 0.5rem;
    }
    p {
      color: #64748b;
    }
  `]
})
export class PlaceholderPageComponent {
  @Input() icon = '🚧';
  @Input() title = 'Page en construction';
  @Input() description = 'Cette fonctionnalité sera bientôt disponible';
}
