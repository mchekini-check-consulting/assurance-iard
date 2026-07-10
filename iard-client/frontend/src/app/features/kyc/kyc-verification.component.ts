import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { KycService } from '../../core/services/kyc.service';
import {
  KycStatusResponse,
  TitreSejourExtraction,
  RibExtraction,
  KycVerificationResult,
  StatutKyc
} from '../../core/models/kyc.model';

@Component({
  selector: 'app-kyc-verification',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="kyc-container">
      <header class="kyc-header">
        <a routerLink="/dashboard" class="logo">
          <span class="logo-icon">🛡️</span>
          <span class="logo-text">IARD Assurances</span>
        </a>
      </header>

      <main class="kyc-content">
        <div class="kyc-title">
          <h1>Vérification d'identité (KYC)</h1>
          <p>Pour finaliser votre souscription, nous devons vérifier votre identité.</p>
        </div>

        @if (isLoading()) {
          <div class="loading">
            <div class="spinner"></div>
            <p>Chargement...</p>
          </div>
        } @else if (kycStatus()?.statut === 'VERIFIE') {
          <div class="success-card">
            <span class="success-icon">✅</span>
            <h2>Identité vérifiée</h2>
            <p>Votre KYC a été validé le {{ kycStatus()!.dateVerification | date:'dd/MM/yyyy à HH:mm' }}</p>
            <button class="btn btn-primary" (click)="continueToContract()">
              Continuer vers le contrat
            </button>
          </div>
        } @else {
          <!-- Étapes -->
          <div class="steps">
            <div class="step" [class.completed]="titreSejourExtraction()?.extractionReussie" [class.active]="currentStep() === 1">
              <span class="step-number">1</span>
              <span class="step-label">Titre de séjour</span>
            </div>
            <div class="step-line"></div>
            <div class="step" [class.completed]="ribExtraction()?.extractionReussie" [class.active]="currentStep() === 2">
              <span class="step-number">2</span>
              <span class="step-label">RIB</span>
            </div>
            <div class="step-line"></div>
            <div class="step" [class.active]="currentStep() === 3">
              <span class="step-number">3</span>
              <span class="step-label">Vérification</span>
            </div>
          </div>

          <!-- Étape 1: Titre de séjour -->
          @if (currentStep() === 1) {
            <div class="upload-card">
              <h2>📄 Titre de séjour</h2>
              <p>Téléversez une photo ou un scan de votre titre de séjour en cours de validité.</p>

              <div class="upload-zone"
                   [class.dragging]="isDragging()"
                   (dragover)="onDragOver($event)"
                   (dragleave)="onDragLeave($event)"
                   (drop)="onDrop($event, 'titre')">
                <input type="file"
                       id="titreFile"
                       accept="image/*,.pdf"
                       (change)="onFileSelected($event, 'titre')"
                       hidden>
                <label for="titreFile" class="upload-label">
                  <span class="upload-icon">📤</span>
                  <span class="upload-text">Cliquez ou déposez votre fichier ici</span>
                  <span class="upload-hint">JPG, PNG ou PDF (max 10 MB)</span>
                </label>
              </div>

              @if (isUploading()) {
                <div class="uploading">
                  <div class="spinner-small"></div>
                  <span>Analyse en cours...</span>
                </div>
              }

              @if (uploadError()) {
                <div class="error-message">{{ uploadError() }}</div>
              }

              @if (titreSejourExtraction()?.extractionReussie) {
                <div class="extraction-result">
                  <h3>Données extraites</h3>
                  <div class="data-grid">
                    <div class="data-item">
                      <span class="label">Nom</span>
                      <span class="value">{{ titreSejourExtraction()!.nom || '-' }}</span>
                    </div>
                    <div class="data-item">
                      <span class="label">Prénom</span>
                      <span class="value">{{ titreSejourExtraction()!.prenom || '-' }}</span>
                    </div>
                    <div class="data-item">
                      <span class="label">N° Document</span>
                      <span class="value">{{ titreSejourExtraction()!.numero || '-' }}</span>
                    </div>
                    <div class="data-item">
                      <span class="label">Date d'expiration</span>
                      <span class="value">{{ titreSejourExtraction()!.dateExpiration || '-' }}</span>
                    </div>
                  </div>
                  <button class="btn btn-primary" (click)="nextStep()">Continuer</button>
                </div>
              }
            </div>
          }

          <!-- Étape 2: RIB -->
          @if (currentStep() === 2) {
            <div class="upload-card">
              <h2>🏦 Relevé d'Identité Bancaire (RIB)</h2>
              <p>Téléversez une photo ou un scan de votre RIB.</p>

              <div class="upload-zone"
                   [class.dragging]="isDragging()"
                   (dragover)="onDragOver($event)"
                   (dragleave)="onDragLeave($event)"
                   (drop)="onDrop($event, 'rib')">
                <input type="file"
                       id="ribFile"
                       accept="image/*,.pdf"
                       (change)="onFileSelected($event, 'rib')"
                       hidden>
                <label for="ribFile" class="upload-label">
                  <span class="upload-icon">📤</span>
                  <span class="upload-text">Cliquez ou déposez votre fichier ici</span>
                  <span class="upload-hint">JPG, PNG ou PDF (max 10 MB)</span>
                </label>
              </div>

              @if (isUploading()) {
                <div class="uploading">
                  <div class="spinner-small"></div>
                  <span>Analyse en cours...</span>
                </div>
              }

              @if (uploadError()) {
                <div class="error-message">{{ uploadError() }}</div>
              }

              @if (ribExtraction()?.extractionReussie) {
                <div class="extraction-result">
                  <h3>Données extraites</h3>
                  <div class="data-grid">
                    <div class="data-item">
                      <span class="label">Nom</span>
                      <span class="value">{{ ribExtraction()!.nom || '-' }}</span>
                    </div>
                    <div class="data-item">
                      <span class="label">Prénom</span>
                      <span class="value">{{ ribExtraction()!.prenom || '-' }}</span>
                    </div>
                    <div class="data-item">
                      <span class="label">Banque</span>
                      <span class="value">{{ ribExtraction()!.banque || '-' }}</span>
                    </div>
                    <div class="data-item full-width">
                      <span class="label">IBAN</span>
                      <span class="value monospace">{{ ribExtraction()!.iban || '-' }}</span>
                    </div>
                  </div>
                  <div class="button-group">
                    <button class="btn btn-outline" (click)="previousStep()">Retour</button>
                    <button class="btn btn-primary" (click)="nextStep()">Continuer</button>
                  </div>
                </div>
              }
            </div>
          }

          <!-- Étape 3: Vérification -->
          @if (currentStep() === 3) {
            <div class="verification-card">
              <h2>🔍 Vérification</h2>
              <p>Nous allons maintenant vérifier que les informations extraites correspondent à votre profil.</p>

              <div class="summary-section">
                <h3>Récapitulatif</h3>
                <div class="summary-grid">
                  <div class="summary-item">
                    <span class="summary-label">📄 Titre de séjour</span>
                    <span class="summary-value">{{ titreSejourExtraction()!.nom }} {{ titreSejourExtraction()!.prenom }}</span>
                  </div>
                  <div class="summary-item">
                    <span class="summary-label">🏦 RIB</span>
                    <span class="summary-value">{{ ribExtraction()!.banque }} - {{ ribExtraction()!.iban }}</span>
                  </div>
                </div>
              </div>

              @if (verificationResult()) {
                @if (verificationResult()!.success) {
                  <div class="result-success">
                    <span class="result-icon">✅</span>
                    <h3>Vérification réussie</h3>
                    <p>{{ verificationResult()!.message }}</p>
                    <button class="btn btn-primary" (click)="continueToContract()">
                      Continuer vers le contrat
                    </button>
                  </div>
                } @else {
                  <div class="result-error">
                    <span class="result-icon">❌</span>
                    <h3>Vérification échouée</h3>
                    <ul class="error-list">
                      @for (erreur of verificationResult()!.erreurs; track erreur) {
                        <li>{{ erreur }}</li>
                      }
                    </ul>
                    <p class="retry-hint">Vous pouvez téléverser à nouveau vos documents et réessayer.</p>
                    <button class="btn btn-outline" (click)="restart()">
                      Recommencer
                    </button>
                  </div>
                }
              } @else {
                <div class="button-group">
                  <button class="btn btn-outline" (click)="previousStep()">Retour</button>
                  <button class="btn btn-primary" [disabled]="isVerifying()" (click)="verify()">
                    @if (isVerifying()) {
                      <span class="spinner-small"></span> Vérification...
                    } @else {
                      Lancer la vérification
                    }
                  </button>
                </div>
              }
            </div>
          }
        }
      </main>
    </div>
  `,
  styles: [`
    .kyc-container {
      min-height: 100vh;
      background: #f8fafc;
    }

    .kyc-header {
      padding: 1rem 2rem;
      background: white;
      border-bottom: 1px solid #e2e8f0;

      .logo {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        text-decoration: none;
        font-weight: 700;
        color: #1e293b;
        .logo-icon { font-size: 1.5rem; }
      }
    }

    .kyc-content {
      max-width: 700px;
      margin: 0 auto;
      padding: 2rem;
    }

    .kyc-title {
      text-align: center;
      margin-bottom: 2rem;

      h1 { color: #1e293b; margin-bottom: 0.5rem; }
      p { color: #64748b; }
    }

    .steps {
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 2rem;
    }

    .step {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;

      .step-number {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: #e2e8f0;
        color: #64748b;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
      }

      .step-label {
        font-size: 0.875rem;
        color: #64748b;
      }

      &.active .step-number {
        background: #2563eb;
        color: white;
      }

      &.completed .step-number {
        background: #10b981;
        color: white;
      }
    }

    .step-line {
      width: 60px;
      height: 2px;
      background: #e2e8f0;
      margin: 0 1rem;
      margin-bottom: 1.5rem;
    }

    .upload-card, .verification-card, .success-card {
      background: white;
      border-radius: 16px;
      padding: 2rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      h2 { color: #1e293b; margin-bottom: 0.5rem; }
      p { color: #64748b; margin-bottom: 1.5rem; }
    }

    .upload-zone {
      border: 2px dashed #e2e8f0;
      border-radius: 12px;
      padding: 3rem 2rem;
      text-align: center;
      transition: all 0.2s;
      cursor: pointer;

      &:hover, &.dragging {
        border-color: #2563eb;
        background: #eff6ff;
      }
    }

    .upload-label {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
      cursor: pointer;

      .upload-icon { font-size: 2.5rem; }
      .upload-text { font-weight: 500; color: #1e293b; }
      .upload-hint { font-size: 0.875rem; color: #94a3b8; }
    }

    .uploading {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 0.75rem;
      padding: 1rem;
      margin-top: 1rem;
      color: #64748b;
    }

    .error-message {
      background: #fef2f2;
      color: #dc2626;
      padding: 1rem;
      border-radius: 8px;
      margin-top: 1rem;
    }

    .extraction-result {
      margin-top: 1.5rem;
      padding-top: 1.5rem;
      border-top: 1px solid #e2e8f0;

      h3 { color: #1e293b; margin-bottom: 1rem; font-size: 1rem; }
    }

    .data-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 1rem;
      margin-bottom: 1.5rem;

      @media (max-width: 500px) {
        grid-template-columns: 1fr;
      }
    }

    .data-item {
      padding: 0.75rem;
      background: #f8fafc;
      border-radius: 8px;

      &.full-width { grid-column: 1 / -1; }

      .label {
        display: block;
        font-size: 0.75rem;
        color: #64748b;
        margin-bottom: 0.25rem;
        text-transform: uppercase;
        letter-spacing: 0.5px;
      }

      .value {
        font-weight: 500;
        color: #1e293b;
      }

      .monospace {
        font-family: monospace;
        font-size: 0.9rem;
      }
    }

    .summary-section {
      margin-bottom: 1.5rem;
      padding: 1.5rem;
      background: #f8fafc;
      border-radius: 12px;

      h3 { margin-bottom: 1rem; color: #1e293b; }
    }

    .summary-grid {
      display: flex;
      flex-direction: column;
      gap: 0.75rem;
    }

    .summary-item {
      display: flex;
      justify-content: space-between;
      padding: 0.5rem 0;
      border-bottom: 1px solid #e2e8f0;

      &:last-child { border-bottom: none; }

      .summary-label { color: #64748b; }
      .summary-value { color: #1e293b; font-weight: 500; text-align: right; }
    }

    .result-success, .result-error {
      text-align: center;
      padding: 2rem;

      .result-icon { font-size: 3rem; display: block; margin-bottom: 1rem; }
      h3 { margin-bottom: 0.5rem; }
      p { color: #64748b; }
    }

    .result-success {
      background: #f0fdf4;
      border-radius: 12px;
    }

    .result-error {
      background: #fef2f2;
      border-radius: 12px;

      .error-list {
        text-align: left;
        padding: 1rem;
        background: white;
        border-radius: 8px;
        margin: 1rem 0;

        li {
          color: #dc2626;
          margin-bottom: 0.5rem;
          &:last-child { margin-bottom: 0; }
        }
      }

      .retry-hint {
        font-size: 0.9rem;
        margin-bottom: 1rem;
      }
    }

    .success-card {
      text-align: center;
      .success-icon { font-size: 4rem; display: block; margin-bottom: 1rem; }
    }

    .button-group {
      display: flex;
      gap: 1rem;
      justify-content: center;
    }

    .btn {
      padding: 0.875rem 1.5rem;
      border-radius: 8px;
      font-weight: 600;
      cursor: pointer;
      border: none;
      transition: all 0.2s;
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;

      &.btn-primary {
        background: #2563eb;
        color: white;
        &:hover:not(:disabled) { background: #1d4ed8; }
        &:disabled { opacity: 0.6; cursor: not-allowed; }
      }

      &.btn-outline {
        background: white;
        border: 2px solid #e2e8f0;
        color: #1e293b;
        &:hover { border-color: #2563eb; color: #2563eb; }
      }
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 4rem;
      p { margin-top: 1rem; color: #64748b; }
    }

    .spinner, .spinner-small {
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #e2e8f0;
      border-top-color: #2563eb;
    }

    .spinner-small {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(0, 0, 0, 0.1);
      border-top-color: currentColor;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class KycVerificationComponent implements OnInit {
  kycStatus = signal<KycStatusResponse | null>(null);
  isLoading = signal(true);
  currentStep = signal(1);
  isDragging = signal(false);
  isUploading = signal(false);
  isVerifying = signal(false);
  uploadError = signal<string | null>(null);

  titreSejourExtraction = signal<TitreSejourExtraction | null>(null);
  ribExtraction = signal<RibExtraction | null>(null);
  verificationResult = signal<KycVerificationResult | null>(null);

  private devisId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private kycService: KycService
  ) {}

  ngOnInit(): void {
    this.devisId = this.route.snapshot.queryParams['devisId'] ?
      +this.route.snapshot.queryParams['devisId'] : null;

    this.loadKycStatus();
  }

  private loadKycStatus(): void {
    this.kycService.getStatus().subscribe({
      next: (status) => {
        this.kycStatus.set(status);
        this.isLoading.set(false);

        // Si des documents ont déjà été uploadés
        if (status.donneesExtraites) {
          if (status.donneesExtraites.titreSejour_nom) {
            this.titreSejourExtraction.set({
              nom: status.donneesExtraites.titreSejour_nom,
              prenom: status.donneesExtraites.titreSejour_prenom,
              numero: status.donneesExtraites.titreSejour_numero,
              dateExpiration: status.donneesExtraites.titreSejour_dateExpiration,
              extractionReussie: true
            });
          }
          if (status.donneesExtraites.rib_nom) {
            this.ribExtraction.set({
              nom: status.donneesExtraites.rib_nom,
              prenom: status.donneesExtraites.rib_prenom,
              banque: status.donneesExtraites.rib_banque,
              iban: status.donneesExtraites.rib_iban,
              extractionReussie: true
            });
          }

          // Déterminer l'étape courante
          if (this.titreSejourExtraction()?.extractionReussie && this.ribExtraction()?.extractionReussie) {
            this.currentStep.set(3);
          } else if (this.titreSejourExtraction()?.extractionReussie) {
            this.currentStep.set(2);
          }
        }
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(true);
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(false);
  }

  onDrop(event: DragEvent, type: 'titre' | 'rib'): void {
    event.preventDefault();
    this.isDragging.set(false);

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.uploadFile(files[0], type);
    }
  }

  onFileSelected(event: Event, type: 'titre' | 'rib'): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.uploadFile(input.files[0], type);
    }
  }

  private uploadFile(file: File, type: 'titre' | 'rib'): void {
    this.uploadError.set(null);
    this.isUploading.set(true);

    const handleError = (err: { error?: { message?: string } }) => {
      this.isUploading.set(false);
      this.uploadError.set(err.error?.message || 'Erreur lors du téléversement');
    };

    if (type === 'titre') {
      this.kycService.uploadTitreSejour(file).subscribe({
        next: (result) => {
          this.isUploading.set(false);
          if (result.extractionReussie) {
            this.titreSejourExtraction.set(result);
          } else {
            this.uploadError.set(result.erreur || 'Erreur lors de l\'extraction');
          }
        },
        error: handleError
      });
    } else {
      this.kycService.uploadRib(file).subscribe({
        next: (result) => {
          this.isUploading.set(false);
          if (result.extractionReussie) {
            this.ribExtraction.set(result);
          } else {
            this.uploadError.set(result.erreur || 'Erreur lors de l\'extraction');
          }
        },
        error: handleError
      });
    }
  }

  nextStep(): void {
    this.uploadError.set(null);
    this.currentStep.update(s => s + 1);
  }

  previousStep(): void {
    this.uploadError.set(null);
    this.verificationResult.set(null);
    this.currentStep.update(s => s - 1);
  }

  restart(): void {
    this.currentStep.set(1);
    this.titreSejourExtraction.set(null);
    this.ribExtraction.set(null);
    this.verificationResult.set(null);
    this.uploadError.set(null);
  }

  verify(): void {
    this.isVerifying.set(true);

    this.kycService.verify().subscribe({
      next: (result) => {
        this.isVerifying.set(false);
        this.verificationResult.set(result);

        if (result.success) {
          // Recharger le statut
          this.kycStatus.update(s => s ? { ...s, statut: StatutKyc.VERIFIE } : s);
        }
      },
      error: (err) => {
        this.isVerifying.set(false);
        this.verificationResult.set({
          statut: StatutKyc.REFUSE,
          success: false,
          erreurs: [err.error?.message || 'Erreur lors de la vérification'],
          message: 'Erreur lors de la vérification'
        });
      }
    });
  }

  continueToContract(): void {
    if (this.devisId) {
      this.router.navigate(['/devis', this.devisId, 'resultat']);
    } else {
      this.router.navigate(['/dashboard/devis']);
    }
  }
}
