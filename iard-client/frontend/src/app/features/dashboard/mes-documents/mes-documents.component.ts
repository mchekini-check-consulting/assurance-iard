import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { KycService } from '../../../core/services/kyc.service';
import { DocumentResponse, TypeDocument, KycStatusResponse, StatutKyc } from '../../../core/models/kyc.model';

@Component({
  selector: 'app-mes-documents',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="mes-documents">
      <div class="page-header">
        <div>
          <h1>Mes documents</h1>
          <p>Consultez vos documents et votre statut KYC</p>
        </div>
        <a routerLink="/kyc" class="btn btn-primary">
          + Téléverser des documents
        </a>
      </div>

      <!-- Statut KYC -->
      <div class="kyc-status-card" [class]="'status-' + kycStatus()?.statut?.toLowerCase()">
        <div class="status-header">
          <h3>Statut de vérification (KYC)</h3>
          <span class="status-badge" [class]="getStatutClass(kycStatus()?.statut)">
            {{ getStatutLabel(kycStatus()?.statut) }}
          </span>
        </div>

        @if (kycStatus()?.statut === 'VERIFIE') {
          <p class="status-detail">
            Identité vérifiée le {{ kycStatus()!.dateVerification | date:'dd/MM/yyyy à HH:mm' }}
          </p>
        } @else if (kycStatus()?.statut === 'REFUSE') {
          <p class="status-detail error">
            {{ kycStatus()!.motifRefus }}
          </p>
          <a routerLink="/kyc" class="btn btn-outline btn-sm">
            Réessayer la vérification
          </a>
        } @else if (kycStatus()?.statut === 'EN_COURS') {
          <p class="status-detail">
            Documents téléversés, en attente de vérification
          </p>
          <a routerLink="/kyc" class="btn btn-primary btn-sm">
            Finaliser la vérification
          </a>
        } @else {
          <p class="status-detail">
            Aucune vérification d'identité effectuée
          </p>
          <a routerLink="/kyc" class="btn btn-primary btn-sm">
            Commencer la vérification
          </a>
        }
      </div>

      <!-- Liste des documents -->
      @if (isLoading()) {
        <div class="loading">
          <div class="spinner"></div>
          <p>Chargement...</p>
        </div>
      } @else if (documents().length === 0) {
        <div class="empty-state">
          <span class="empty-icon">📁</span>
          <h2>Aucun document</h2>
          <p>Vous n'avez pas encore téléversé de documents.</p>
          <a routerLink="/kyc" class="btn btn-primary">
            Téléverser mes documents
          </a>
        </div>
      } @else {
        <div class="documents-list">
          @for (doc of documents(); track doc.id) {
            <div class="document-card">
              <div class="document-icon">
                {{ getDocumentIcon(doc.type) }}
              </div>
              <div class="document-info">
                <span class="document-type">{{ getDocumentTypeLabel(doc.type) }}</span>
                <span class="document-name">{{ doc.nomFichier }}</span>
                <span class="document-date">Ajouté le {{ doc.createdAt | date:'dd/MM/yyyy' }}</span>
              </div>
              <div class="document-actions">
                <a [href]="getDocumentUrl(doc.id)" target="_blank" class="btn-icon" title="Voir">
                  👁️
                </a>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    $primary-color: #2563eb;
    $text-color: #1e293b;
    $text-muted: #64748b;
    $border-color: #e2e8f0;

    .mes-documents {
      max-width: 900px;
    }

    .page-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 2rem;

      h1 {
        font-size: 1.5rem;
        color: $text-color;
        margin-bottom: 0.25rem;
      }

      p { color: $text-muted; }

      @media (max-width: 500px) {
        flex-direction: column;
        gap: 1rem;
      }
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border-radius: 8px;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
      display: inline-block;

      &.btn-primary {
        background: $primary-color;
        color: white;
        &:hover { background: darken($primary-color, 10%); }
      }

      &.btn-outline {
        background: white;
        border: 2px solid $border-color;
        color: $text-color;
        &:hover { border-color: $primary-color; color: $primary-color; }
      }

      &.btn-sm {
        padding: 0.5rem 1rem;
        font-size: 0.875rem;
      }
    }

    .kyc-status-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 2rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      .status-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;

        h3 { margin: 0; color: $text-color; }
      }

      .status-detail {
        color: $text-muted;
        margin-bottom: 1rem;

        &.error { color: #dc2626; }
      }
    }

    .status-badge {
      padding: 0.25rem 0.75rem;
      border-radius: 20px;
      font-size: 0.8rem;
      font-weight: 600;

      &.badge-success {
        background: #d1fae5;
        color: #047857;
      }

      &.badge-warning {
        background: #fef3c7;
        color: #b45309;
      }

      &.badge-error {
        background: #fee2e2;
        color: #b91c1c;
      }

      &.badge-muted {
        background: #f1f5f9;
        color: $text-muted;
      }
    }

    .documents-list {
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .document-card {
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
    }

    .document-icon {
      font-size: 2rem;
      width: 50px;
      height: 50px;
      background: #f1f5f9;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .document-info {
      flex: 1;

      .document-type {
        display: block;
        font-weight: 600;
        color: $text-color;
      }

      .document-name {
        display: block;
        color: $text-muted;
        font-size: 0.9rem;
      }

      .document-date {
        display: block;
        color: $text-muted;
        font-size: 0.8rem;
        margin-top: 0.25rem;
      }
    }

    .document-actions {
      .btn-icon {
        background: none;
        border: none;
        font-size: 1.25rem;
        cursor: pointer;
        padding: 0.5rem;
        text-decoration: none;
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
      p { color: $text-muted; margin-bottom: 1.5rem; }
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
  `]
})
export class MesDocumentsComponent implements OnInit {
  documents = signal<DocumentResponse[]>([]);
  kycStatus = signal<KycStatusResponse | null>(null);
  isLoading = signal(true);

  constructor(private kycService: KycService) {}

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.kycService.getStatus().subscribe({
      next: (status) => {
        this.kycStatus.set(status);
      }
    });

    this.kycService.getDocuments().subscribe({
      next: (docs) => {
        this.documents.set(docs);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  getDocumentIcon(type: TypeDocument): string {
    switch (type) {
      case TypeDocument.TITRE_SEJOUR: return '📄';
      case TypeDocument.RIB: return '🏦';
      default: return '📁';
    }
  }

  getDocumentTypeLabel(type: TypeDocument): string {
    switch (type) {
      case TypeDocument.TITRE_SEJOUR: return 'Titre de séjour';
      case TypeDocument.RIB: return 'RIB';
      default: return type;
    }
  }

  getStatutLabel(statut?: StatutKyc): string {
    switch (statut) {
      case StatutKyc.NON_VERIFIE: return 'Non vérifié';
      case StatutKyc.EN_COURS: return 'En cours';
      case StatutKyc.VERIFIE: return 'Vérifié';
      case StatutKyc.REFUSE: return 'Refusé';
      default: return 'Non vérifié';
    }
  }

  getStatutClass(statut?: StatutKyc): string {
    switch (statut) {
      case StatutKyc.VERIFIE: return 'badge-success';
      case StatutKyc.EN_COURS: return 'badge-warning';
      case StatutKyc.REFUSE: return 'badge-error';
      default: return 'badge-muted';
    }
  }

  getDocumentUrl(id: number): string {
    return this.kycService.getDocumentUrl(id);
  }
}
