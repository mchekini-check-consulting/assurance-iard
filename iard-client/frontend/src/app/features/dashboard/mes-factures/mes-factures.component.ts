import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { FactureService } from '../../../core/services/facture.service';
import { Facture } from '../../../core/models/facture.model';

@Component({
  selector: 'app-mes-factures',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="mes-factures">
      <div class="page-header">
        <div>
          <h1>Facturation</h1>
          <p>Consultez et téléchargez vos factures</p>
        </div>
      </div>

      @if (isLoading()) {
        <div class="loading">
          <div class="spinner"></div>
          <p>Chargement...</p>
        </div>
      } @else if (factures().length === 0) {
        <div class="empty-state">
          <span class="empty-icon">📋</span>
          <h2>Aucune facture</h2>
          <p>Vos factures apparaîtront ici après le premier prélèvement réussi.</p>
        </div>
      } @else {
        <div class="factures-list">
          @for (facture of factures(); track facture.id) {
            <div class="facture-card">
              <div class="facture-icon">
                <span>📄</span>
              </div>
              <div class="facture-info">
                <div class="facture-header">
                  <span class="facture-numero">{{ facture.numeroFacture }}</span>
                  <span class="facture-badge badge-success">Payée</span>
                </div>
                <div class="facture-details">
                  <span class="detail">Contrat {{ facture.numeroContrat }}</span>
                  <span class="separator">•</span>
                  <span class="detail">{{ formatPeriode(facture.periode) }}</span>
                  <span class="separator">•</span>
                  <span class="detail">Émise le {{ formatDate(facture.dateEmission) }}</span>
                </div>
              </div>
              <div class="facture-montant">
                <span class="montant">{{ facture.montantTTC | number:'1.2-2' }} €</span>
                <span class="label">TTC</span>
              </div>
              <div class="facture-actions">
                <button class="btn-icon" title="Voir la facture" (click)="voirFacture(facture)">
                  👁️
                </button>
                <button class="btn-icon" title="Télécharger" (click)="telechargerFacture(facture)">
                  ⬇️
                </button>
              </div>
            </div>
          }
        </div>

        <!-- Récapitulatif -->
        <div class="recap-card">
          <h3>Récapitulatif</h3>
          <div class="recap-content">
            <div class="recap-item">
              <span class="recap-label">Nombre de factures</span>
              <span class="recap-value">{{ factures().length }}</span>
            </div>
            <div class="recap-item">
              <span class="recap-label">Total payé</span>
              <span class="recap-value">{{ totalPaye() | number:'1.2-2' }} €</span>
            </div>
          </div>
        </div>
      }

      <!-- Modal de visualisation -->
      @if (selectedFacture()) {
        <div class="modal-overlay" (click)="fermerModal()">
          <div class="modal-content" (click)="$event.stopPropagation()">
            <div class="modal-header">
              <h2>Facture {{ selectedFacture()!.numeroFacture }}</h2>
              <button class="btn-close" (click)="fermerModal()">✕</button>
            </div>
            <div class="modal-body">
              @if (pdfBlobUrl()) {
                <iframe [src]="pdfBlobUrl()" class="pdf-viewer"></iframe>
              } @else {
                <div class="loading">
                  <div class="spinner"></div>
                  <p>Chargement du PDF...</p>
                </div>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    $primary-color: #2563eb;
    $text-color: #1e293b;
    $text-muted: #64748b;
    $border-color: #e2e8f0;

    .mes-factures {
      max-width: 900px;
    }

    .page-header {
      margin-bottom: 2rem;

      h1 {
        font-size: 1.5rem;
        color: $text-color;
        margin-bottom: 0.25rem;
      }

      p { color: $text-muted; }
    }

    .factures-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      margin-bottom: 2rem;
    }

    .facture-card {
      display: flex;
      align-items: center;
      gap: 1rem;
      background: white;
      border-radius: 12px;
      padding: 1.25rem;
      transition: all 0.2s;

      &:hover {
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      @media (max-width: 600px) {
        flex-wrap: wrap;
      }
    }

    .facture-icon {
      font-size: 2rem;
      width: 50px;
      height: 50px;
      background: #eff6ff;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .facture-info {
      flex: 1;

      .facture-header {
        display: flex;
        align-items: center;
        gap: 0.75rem;
        margin-bottom: 0.25rem;
      }

      .facture-numero {
        font-weight: 600;
        color: $text-color;
      }

      .facture-details {
        font-size: 0.875rem;
        color: $text-muted;

        .separator {
          margin: 0 0.5rem;
        }
      }
    }

    .facture-badge {
      padding: 0.25rem 0.5rem;
      border-radius: 12px;
      font-size: 0.75rem;
      font-weight: 600;

      &.badge-success {
        background: #d1fae5;
        color: #047857;
      }
    }

    .facture-montant {
      text-align: right;
      min-width: 100px;

      .montant {
        display: block;
        font-size: 1.25rem;
        font-weight: 700;
        color: $text-color;
      }

      .label {
        font-size: 0.75rem;
        color: $text-muted;
      }
    }

    .facture-actions {
      display: flex;
      gap: 0.5rem;
    }

    .btn-icon {
      background: #f1f5f9;
      border: none;
      font-size: 1.25rem;
      cursor: pointer;
      padding: 0.5rem;
      border-radius: 8px;
      transition: all 0.2s;

      &:hover {
        background: #e2e8f0;
      }
    }

    .recap-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;

      h3 {
        color: $text-color;
        margin-bottom: 1rem;
      }
    }

    .recap-content {
      display: flex;
      gap: 2rem;

      @media (max-width: 500px) {
        flex-direction: column;
        gap: 1rem;
      }
    }

    .recap-item {
      .recap-label {
        display: block;
        font-size: 0.875rem;
        color: $text-muted;
        margin-bottom: 0.25rem;
      }

      .recap-value {
        font-size: 1.5rem;
        font-weight: 700;
        color: $primary-color;
      }
    }

    .empty-state {
      text-align: center;
      padding: 4rem 2rem;
      background: white;
      border-radius: 12px;

      .empty-icon {
        font-size: 4rem;
        display: block;
        margin-bottom: 1rem;
      }

      h2 { color: $text-color; margin-bottom: 0.5rem; }
      p { color: $text-muted; }
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 4rem;

      .spinner {
        width: 40px;
        height: 40px;
        border: 3px solid $border-color;
        border-top-color: $primary-color;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }

      p { margin-top: 1rem; color: $text-muted; }
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    // Modal
    .modal-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      padding: 2rem;
    }

    .modal-content {
      background: white;
      border-radius: 16px;
      width: 100%;
      max-width: 900px;
      max-height: 90vh;
      display: flex;
      flex-direction: column;
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 1.5rem;
      border-bottom: 1px solid $border-color;

      h2 {
        margin: 0;
        font-size: 1.25rem;
        color: $text-color;
      }

      .btn-close {
        background: none;
        border: none;
        font-size: 1.5rem;
        cursor: pointer;
        color: $text-muted;

        &:hover { color: $text-color; }
      }
    }

    .modal-body {
      flex: 1;
      padding: 0;
      overflow: hidden;
    }

    .pdf-viewer {
      width: 100%;
      height: 70vh;
      border: none;
    }
  `]
})
export class MesFacturesComponent implements OnInit {
  factures = signal<Facture[]>([]);
  isLoading = signal(true);
  selectedFacture = signal<Facture | null>(null);
  pdfBlobUrl = signal<SafeResourceUrl | null>(null);
  private currentBlobUrl: string | null = null;

  constructor(
    private factureService: FactureService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.loadFactures();
  }

  loadFactures(): void {
    this.factureService.getFactures().subscribe({
      next: (factures) => {
        this.factures.set(factures);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  totalPaye(): number {
    return this.factures().reduce((sum, f) => sum + f.montantTTC, 0);
  }

  formatPeriode(periode: string): string {
    const [year, month] = periode.split('-');
    const date = new Date(parseInt(year), parseInt(month) - 1);
    return date.toLocaleDateString('fr-FR', { month: 'long', year: 'numeric' });
  }

  formatDate(dateStr: string): string {
    return new Date(dateStr).toLocaleDateString('fr-FR');
  }

  voirFacture(facture: Facture): void {
    this.selectedFacture.set(facture);
    this.pdfBlobUrl.set(null);

    this.factureService.getPdfBlob(facture.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.currentBlobUrl = url;
        this.pdfBlobUrl.set(this.sanitizer.bypassSecurityTrustResourceUrl(url));
      },
      error: () => {
        alert('Erreur lors du chargement du PDF');
        this.fermerModal();
      }
    });
  }

  telechargerFacture(facture: Facture): void {
    this.factureService.getPdfBlob(facture.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${facture.numeroFacture}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        alert('Erreur lors du téléchargement');
      }
    });
  }

  fermerModal(): void {
    if (this.currentBlobUrl) {
      URL.revokeObjectURL(this.currentBlobUrl);
      this.currentBlobUrl = null;
    }
    this.pdfBlobUrl.set(null);
    this.selectedFacture.set(null);
  }
}
