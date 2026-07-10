import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { ContratService } from '../../core/services/contrat.service';
import { Contrat, StatutContrat } from '../../core/models/contrat.model';
import { Formule } from '../../core/models/devis.model';

@Component({
  selector: 'app-contrat-view',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="contrat-container">
      <header class="contrat-header">
        <a routerLink="/dashboard" class="logo">
          <span class="logo-icon">🛡️</span>
          <span class="logo-text">IARD Assurances</span>
        </a>
        <nav class="header-nav">
          <a routerLink="/dashboard/contrats">Mes contrats</a>
        </nav>
      </header>

      @if (isLoading()) {
        <div class="loading">
          <div class="spinner"></div>
          <p>Chargement du contrat...</p>
        </div>
      } @else if (contrat()) {
        <div class="contrat-content">
          <!-- Vue split -->
          <div class="split-view">
            <!-- PDF Viewer -->
            <div class="pdf-panel">
              <div class="pdf-header">
                <h3>Contrat N° {{ contrat()!.numeroContrat }}</h3>
                <button class="btn-download" (click)="downloadPdf()">
                  Télécharger
                </button>
              </div>
              <div class="pdf-viewer">
                @if (pdfBlobUrl()) {
                  <iframe
                    [src]="pdfSafeUrl()"
                    width="100%"
                    height="100%"
                    frameborder="0">
                  </iframe>
                } @else {
                  <div class="pdf-loading">
                    <div class="spinner"></div>
                    <p>Chargement du PDF...</p>
                  </div>
                }
              </div>
            </div>

            <!-- Panel d'information et signature -->
            <div class="info-panel">
              <!-- Statut -->
              <div class="status-card" [class]="'status-' + contrat()!.statut.toLowerCase()">
                @if (contrat()!.statut === 'EN_ATTENTE' && !contrat()!.dateSignature) {
                  <span class="status-icon">⏳</span>
                  <span class="status-text">En attente de signature</span>
                } @else if (contrat()!.statut === 'EN_ATTENTE') {
                  <span class="status-icon">✍️</span>
                  <span class="status-text">Contrat signé — en attente du premier prélèvement</span>
                } @else if (contrat()!.statut === 'ACTIF') {
                  <span class="status-icon">✅</span>
                  <span class="status-text">Contrat actif</span>
                }
              </div>

              <!-- Résumé -->
              <div class="summary-card">
                <h4>Résumé du contrat</h4>
                <div class="summary-item">
                  <span class="label">Formule</span>
                  <span class="value">{{ getFormuleLabel(contrat()!.formule) }}</span>
                </div>
                <div class="summary-item">
                  <span class="label">Produit</span>
                  <span class="value">Assurance Habitation</span>
                </div>
                <div class="summary-item">
                  <span class="label">Prime annuelle TTC</span>
                  <span class="value highlight">{{ contrat()!.primeTTC | number:'1.2-2' }} €</span>
                </div>
                <div class="summary-item">
                  <span class="label">Prime mensuelle</span>
                  <span class="value">{{ contrat()!.primeTTC / 12 | number:'1.2-2' }} €/mois</span>
                </div>
              </div>

              <!-- Bien assuré -->
              <div class="summary-card">
                <h4>Bien assuré</h4>
                <p class="address">
                  {{ contrat()!.donneesRisque.adresse }}<br>
                  {{ contrat()!.donneesRisque.codePostal }} {{ contrat()!.donneesRisque.ville }}
                </p>
                <p class="meta">
                  {{ contrat()!.donneesRisque.typeBien === 'APPARTEMENT' ? 'Appartement' : 'Maison' }} •
                  {{ contrat()!.donneesRisque.surfaceHabitable }} m²
                </p>
              </div>

              <!-- Zone de signature -->
              @if (contrat()!.statut === 'EN_ATTENTE' && !contrat()!.dateSignature) {
                <div class="signature-card">
                  <h4>Signature électronique</h4>
                  <p class="signature-info">
                    Pour finaliser votre souscription, veuillez saisir le code de signature
                    qui vous a été envoyé par SMS.
                  </p>

                  <div class="otp-info">
                    <span class="otp-icon">📱</span>
                    <span>Code de test : <strong>6208</strong></span>
                  </div>

                  <div class="form-group">
                    <label for="otpCode">Code de signature</label>
                    <input
                      type="text"
                      id="otpCode"
                      [(ngModel)]="otpCode"
                      maxlength="4"
                      placeholder="****"
                      class="otp-input"
                      [disabled]="isSigning()">
                  </div>

                  @if (signatureError()) {
                    <div class="error-message">
                      {{ signatureError() }}
                    </div>
                  }

                  <button
                    class="btn-sign"
                    [disabled]="otpCode.length !== 4 || isSigning()"
                    (click)="signer()">
                    @if (isSigning()) {
                      <span class="btn-spinner"></span> Signature en cours...
                    } @else {
                      Signer le contrat
                    }
                  </button>

                  <p class="legal-text">
                    En signant ce contrat, vous acceptez les conditions générales de vente
                    et confirmez avoir pris connaissance de la notice d'information.
                  </p>
                </div>
              } @else if (contrat()!.dateSignature) {
                <div class="signed-card">
                  <h4>Contrat signé</h4>
                  <div class="signed-info">
                    <p><strong>Date de signature :</strong><br>
                    {{ contrat()!.dateSignature | date:'dd/MM/yyyy à HH:mm' }}</p>
                    <p><strong>Référence signature :</strong><br>
                    {{ contrat()!.signatureId }}</p>
                  </div>
                  @if (contrat()!.statut === 'EN_ATTENTE') {
                    <p class="activation-info">
                      Votre contrat sera activé automatiquement après le premier
                      prélèvement de votre prime mensuelle.
                    </p>
                  }
                  <a routerLink="/dashboard/contrats" class="btn-secondary">
                    Voir tous mes contrats
                  </a>
                </div>
              }
            </div>
          </div>
        </div>
      }
    </div>
  `,
  styles: [`
    .contrat-container {
      min-height: 100vh;
      background: #f8fafc;
    }

    .contrat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
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

      .header-nav a {
        color: #64748b;
        text-decoration: none;
        &:hover { color: #2563eb; }
      }
    }

    .contrat-content {
      height: calc(100vh - 65px);
    }

    .split-view {
      display: grid;
      grid-template-columns: 1fr 400px;
      height: 100%;

      @media (max-width: 1024px) {
        grid-template-columns: 1fr;
        height: auto;
      }
    }

    .pdf-panel {
      display: flex;
      flex-direction: column;
      background: #1e293b;
      height: 100%;

      @media (max-width: 1024px) {
        height: 500px;
      }
    }

    .pdf-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 1.5rem;
      background: #0f172a;
      color: white;

      h3 { margin: 0; font-size: 1rem; }

      .btn-download {
        background: transparent;
        border: 1px solid #475569;
        color: white;
        padding: 0.5rem 1rem;
        border-radius: 6px;
        text-decoration: none;
        font-size: 0.875rem;
        cursor: pointer;
        transition: all 0.2s;
        &:hover {
          background: #334155;
          border-color: #64748b;
        }
      }
    }

    .pdf-viewer {
      flex: 1;
      background: #475569;

      iframe {
        display: block;
      }

      .pdf-loading {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        height: 100%;
        color: white;

        .spinner {
          width: 40px;
          height: 40px;
          border: 3px solid rgba(255, 255, 255, 0.3);
          border-top-color: white;
          border-radius: 50%;
          animation: spin 1s linear infinite;
        }

        p {
          margin-top: 1rem;
          opacity: 0.8;
        }
      }
    }

    .info-panel {
      padding: 1.5rem;
      overflow-y: auto;
      background: #f8fafc;

      @media (max-width: 1024px) {
        padding: 1rem;
      }
    }

    .status-card {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem 1.25rem;
      border-radius: 12px;
      margin-bottom: 1.5rem;

      &.status-en_attente {
        background: #fef3c7;
        color: #92400e;
      }

      &.status-actif {
        background: #d1fae5;
        color: #065f46;
      }

      .status-icon { font-size: 1.5rem; }
      .status-text { font-weight: 600; }
    }

    .summary-card, .signature-card, .signed-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 1rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      h4 {
        margin: 0 0 1rem 0;
        color: #1e293b;
        font-size: 1rem;
      }
    }

    .summary-item {
      display: flex;
      justify-content: space-between;
      padding: 0.75rem 0;
      border-bottom: 1px solid #f1f5f9;

      &:last-child { border-bottom: none; }

      .label { color: #64748b; }
      .value { color: #1e293b; font-weight: 500; }
      .value.highlight {
        color: #2563eb;
        font-weight: 700;
        font-size: 1.1rem;
      }
    }

    .address {
      color: #1e293b;
      margin: 0 0 0.5rem 0;
      line-height: 1.5;
    }

    .meta {
      color: #64748b;
      font-size: 0.875rem;
      margin: 0;
    }

    .signature-info {
      color: #64748b;
      font-size: 0.9rem;
      margin: 0 0 1rem 0;
      line-height: 1.5;
    }

    .otp-info {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      background: #eff6ff;
      border-radius: 8px;
      margin-bottom: 1.25rem;
      color: #1e40af;
      font-size: 0.9rem;

      .otp-icon { font-size: 1.25rem; }
    }

    .form-group {
      margin-bottom: 1rem;

      label {
        display: block;
        color: #374151;
        font-weight: 500;
        margin-bottom: 0.5rem;
        font-size: 0.875rem;
      }
    }

    .otp-input {
      width: 100%;
      padding: 1rem;
      font-size: 1.5rem;
      text-align: center;
      letter-spacing: 0.5em;
      border: 2px solid #e2e8f0;
      border-radius: 8px;
      outline: none;
      transition: border-color 0.2s;

      &:focus {
        border-color: #2563eb;
      }

      &:disabled {
        background: #f8fafc;
        cursor: not-allowed;
      }
    }

    .error-message {
      background: #fef2f2;
      color: #dc2626;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      margin-bottom: 1rem;
      font-size: 0.9rem;
    }

    .btn-sign {
      width: 100%;
      padding: 1rem;
      background: #2563eb;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: background 0.2s;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover:not(:disabled) {
        background: #1d4ed8;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }

      .btn-spinner {
        display: inline-block;
        width: 18px;
        height: 18px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top-color: white;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-right: 8px;
      }
    }

    .legal-text {
      margin: 1rem 0 0 0;
      font-size: 0.8rem;
      color: #94a3b8;
      line-height: 1.5;
    }

    .signed-info {
      p {
        margin: 0 0 0.75rem 0;
        color: #1e293b;
        font-size: 0.9rem;
        line-height: 1.5;
      }
    }

    .activation-info {
      margin: 0 0 1rem 0;
      padding: 0.75rem 1rem;
      background: #fffbeb;
      border: 1px solid #fde68a;
      border-radius: 8px;
      color: #92400e;
      font-size: 0.85rem;
      line-height: 1.5;
    }

    .btn-secondary {
      display: block;
      width: 100%;
      padding: 0.875rem 1rem;
      background: white;
      color: #2563eb;
      border: 2px solid #2563eb;
      border-radius: 8px;
      text-align: center;
      text-decoration: none;
      font-weight: 600;
      transition: all 0.2s;
      margin-top: 1rem;

      &:hover {
        background: #eff6ff;
      }
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 4rem;

      .spinner {
        width: 40px;
        height: 40px;
        border: 3px solid #e2e8f0;
        border-top-color: #2563eb;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }
      p { margin-top: 1rem; color: #64748b; }
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }
  `]
})
export class ContratViewComponent implements OnInit {
  contrat = signal<Contrat | null>(null);
  isLoading = signal(true);
  isSigning = signal(false);
  signatureError = signal<string | null>(null);
  otpCode = '';
  pdfBlobUrl = signal<string | null>(null);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contratService: ContratService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    const contratId = this.route.snapshot.params['id'];
    if (contratId) {
      this.loadContrat(+contratId);
    } else {
      this.router.navigate(['/dashboard/contrats']);
    }
  }

  private loadContrat(id: number): void {
    this.contratService.getContrat(id).subscribe({
      next: (contrat) => {
        this.contrat.set(contrat);
        this.loadPdf(id);
      },
      error: () => {
        this.router.navigate(['/dashboard/contrats']);
      }
    });
  }

  private loadPdf(id: number): void {
    this.contratService.getPdfBlob(id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.pdfBlobUrl.set(url);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  pdfUrl(): string {
    return this.pdfBlobUrl() || '';
  }

  pdfSafeUrl(): SafeResourceUrl {
    return this.sanitizer.bypassSecurityTrustResourceUrl(this.pdfUrl());
  }

  getFormuleLabel(formule: Formule): string {
    switch (formule) {
      case Formule.ESSENTIELLE: return 'Formule Essentielle';
      case Formule.CONFORT: return 'Formule Confort';
      case Formule.PREMIUM: return 'Formule Premium';
      default: return formule;
    }
  }

  signer(): void {
    const c = this.contrat();
    if (!c || this.otpCode.length !== 4) return;

    this.isSigning.set(true);
    this.signatureError.set(null);

    this.contratService.signerContrat(c.id, this.otpCode).subscribe({
      next: (contrat) => {
        this.contrat.set(contrat);
        this.isSigning.set(false);
        // Recharger le PDF signé
        this.loadPdf(contrat.id);
      },
      error: (err) => {
        console.error('Erreur de signature:', err);
        this.signatureError.set(
          err.error?.message || 'Code de signature invalide. Veuillez réessayer.'
        );
        this.isSigning.set(false);
        this.otpCode = '';
      }
    });
  }

  downloadPdf(): void {
    const url = this.pdfBlobUrl();
    if (url) {
      const a = document.createElement('a');
      a.href = url;
      a.download = `contrat_${this.contrat()?.numeroContrat || 'document'}.pdf`;
      a.click();
    }
  }
}
