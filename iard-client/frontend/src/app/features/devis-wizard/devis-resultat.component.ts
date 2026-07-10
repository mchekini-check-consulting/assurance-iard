import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DevisService } from '../../core/services/devis.service';
import { ContratService } from '../../core/services/contrat.service';
import { KycService } from '../../core/services/kyc.service';
import { Devis, Formule, StatutDevis } from '../../core/models/devis.model';
import { StatutKyc } from '../../core/models/kyc.model';

@Component({
  selector: 'app-devis-resultat',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="resultat-container">
      <header class="resultat-header">
        <a routerLink="/dashboard" class="logo">
          <span class="logo-icon">🛡️</span>
          <span class="logo-text">IARD Assurances</span>
        </a>
      </header>

      @if (isLoading()) {
        <div class="loading">
          <div class="spinner"></div>
          <p>Chargement du devis...</p>
        </div>
      } @else if (devis()) {
        <main class="resultat-content">
          <div class="success-banner">
            <span class="success-icon">✅</span>
            <h1>Votre devis est prêt !</h1>
            <p>Référence : DEV-{{ devis()!.id }}</p>
          </div>

          <div class="resultat-grid">
            <!-- Résumé tarif -->
            <div class="tarif-card">
              <div class="tarif-header">
                <h2>{{ getFormuleLabel(devis()!.resultatTarif!.formule) }}</h2>
                <span class="tarif-badge">Assurance Habitation</span>
              </div>

              <div class="tarif-main">
                <div class="tarif-annual">
                  <span class="tarif-label">Prime annuelle TTC</span>
                  <span class="tarif-value">{{ devis()!.resultatTarif!.primeTTC | number:'1.2-2' }} €</span>
                </div>
                <div class="tarif-monthly">
                  soit <strong>{{ devis()!.resultatTarif!.primeMensuelle | number:'1.2-2' }} €/mois</strong>
                </div>
              </div>

              <div class="tarif-details">
                <div class="tarif-line">
                  <span>Prime HT</span>
                  <span>{{ devis()!.resultatTarif!.primeHT | number:'1.2-2' }} €</span>
                </div>
                <div class="tarif-line">
                  <span>Taxes et contributions</span>
                  <span>{{ devis()!.resultatTarif!.taxes | number:'1.2-2' }} €</span>
                </div>
                <div class="tarif-line total">
                  <span>Total TTC</span>
                  <span>{{ devis()!.resultatTarif!.primeTTC | number:'1.2-2' }} €</span>
                </div>
              </div>
            </div>

            <!-- Bien assuré -->
            <div class="info-card">
              <h3>🏠 Bien assuré</h3>
              <div class="info-content">
                <p><strong>{{ devis()!.donneesRisque!.adresse }}</strong></p>
                <p>{{ devis()!.donneesRisque!.codePostal }} {{ devis()!.donneesRisque!.ville }}</p>
                <p class="info-meta">
                  {{ devis()!.donneesRisque!.typeBien === 'APPARTEMENT' ? 'Appartement' : 'Maison' }} •
                  {{ devis()!.donneesRisque!.surfaceHabitable }} m² •
                  {{ devis()!.donneesRisque!.nombrePieces }} pièces
                </p>
              </div>
            </div>
          </div>

          <!-- Garanties -->
          <div class="garanties-section">
            <h3>Garanties incluses</h3>
            <div class="garanties-grid">
              @for (garantie of devis()!.resultatTarif!.garantiesIncluses; track garantie.code) {
                <div class="garantie-card incluse">
                  <span class="garantie-icon">✓</span>
                  <div class="garantie-content">
                    <span class="garantie-nom">{{ garantie.libelle }}</span>
                    @if (garantie.plafond) {
                      <span class="garantie-detail">Plafond : {{ garantie.plafond | number }} €</span>
                    }
                    @if (garantie.franchise && garantie.franchise > 0) {
                      <span class="garantie-detail">Franchise : {{ garantie.franchise | number }} €</span>
                    }
                  </div>
                </div>
              }
            </div>

            @if (devis()!.resultatTarif!.garantiesOptionnelles.length > 0) {
              <h3 class="mt-2">Options souscrites</h3>
              <div class="garanties-grid">
                @for (garantie of devis()!.resultatTarif!.garantiesOptionnelles; track garantie.code) {
                  <div class="garantie-card optionnelle">
                    <span class="garantie-icon">+</span>
                    <div class="garantie-content">
                      <span class="garantie-nom">{{ garantie.libelle }}</span>
                      <span class="garantie-prix">+{{ garantie.primeSupplementaire | number:'1.2-2' }} €/an</span>
                    </div>
                  </div>
                }
              </div>
            }
          </div>

          <!-- Actions -->
          <div class="actions-section">
            @if (devis()!.statut !== 'TRANSFORME') {
              <a [routerLink]="['/devis', devis()!.id]" class="btn btn-outline">
                ✏️ Modifier le devis
              </a>
              <button
                class="btn btn-primary btn-lg"
                [disabled]="isGenerating()"
                (click)="souscrire()">
                @if (isGenerating()) {
                  <span class="btn-spinner"></span> Génération en cours...
                } @else {
                  Souscrire en ligne
                }
              </button>
            } @else {
              <a [routerLink]="['/dashboard/contrats']" class="btn btn-primary btn-lg">
                Voir mes contrats
              </a>
            }
          </div>

          <div class="help-section">
            <p>Des questions ? Appelez-nous au <strong>01 23 45 67 89</strong></p>
          </div>
        </main>
      }
    </div>
  `,
  styles: [`
    .resultat-container {
      min-height: 100vh;
      background: #f8fafc;
    }

    .resultat-header {
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

    .resultat-content {
      max-width: 900px;
      margin: 0 auto;
      padding: 2rem;
    }

    .success-banner {
      text-align: center;
      padding: 2rem;
      background: white;
      border-radius: 12px;
      margin-bottom: 2rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      .success-icon { font-size: 3rem; display: block; margin-bottom: 1rem; }
      h1 { font-size: 1.75rem; color: #1e293b; margin-bottom: 0.5rem; }
      p { color: #64748b; }
    }

    .resultat-grid {
      display: grid;
      grid-template-columns: 2fr 1fr;
      gap: 1.5rem;
      margin-bottom: 2rem;

      @media (max-width: 768px) {
        grid-template-columns: 1fr;
      }
    }

    .tarif-card {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      .tarif-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1.5rem;

        h2 { color: #1e293b; margin: 0; }
        .tarif-badge {
          background: #e0f2fe;
          color: #0369a1;
          padding: 0.25rem 0.75rem;
          border-radius: 20px;
          font-size: 0.8rem;
        }
      }

      .tarif-main {
        text-align: center;
        padding: 2rem;
        background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
        border-radius: 12px;
        color: white;
        margin-bottom: 1.5rem;

        .tarif-label { display: block; opacity: 0.9; margin-bottom: 0.5rem; }
        .tarif-value { font-size: 2.5rem; font-weight: 800; }
        .tarif-monthly { margin-top: 0.5rem; opacity: 0.9; }
      }

      .tarif-details {
        .tarif-line {
          display: flex;
          justify-content: space-between;
          padding: 0.75rem 0;
          border-bottom: 1px solid #e2e8f0;
          color: #64748b;

          &.total {
            font-weight: 700;
            color: #1e293b;
            border-bottom: none;
          }
        }
      }
    }

    .info-card {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      h3 { margin-bottom: 1rem; color: #1e293b; }
      .info-content p { margin: 0.25rem 0; color: #1e293b; }
      .info-meta { color: #64748b; font-size: 0.9rem; margin-top: 0.5rem !important; }
    }

    .garanties-section {
      background: white;
      border-radius: 12px;
      padding: 2rem;
      margin-bottom: 2rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      h3 { color: #1e293b; margin-bottom: 1rem; }
      .mt-2 { margin-top: 2rem; }
    }

    .garanties-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 1rem;
    }

    .garantie-card {
      display: flex;
      align-items: flex-start;
      gap: 0.75rem;
      padding: 1rem;
      border-radius: 8px;

      &.incluse {
        background: #f0fdf4;
        .garantie-icon { color: #10b981; }
      }

      &.optionnelle {
        background: #eff6ff;
        .garantie-icon { color: #2563eb; }
      }

      .garantie-content {
        .garantie-nom { display: block; font-weight: 500; color: #1e293b; }
        .garantie-detail { display: block; font-size: 0.85rem; color: #64748b; }
        .garantie-prix { display: block; font-size: 0.85rem; color: #2563eb; margin-top: 0.25rem; }
      }
    }

    .actions-section {
      display: flex;
      gap: 1rem;
      justify-content: center;
      margin-bottom: 2rem;

      @media (max-width: 500px) {
        flex-direction: column;
      }
    }

    .btn {
      padding: 0.875rem 1.5rem;
      border-radius: 8px;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      border: none;
      transition: all 0.2s;

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

      &.btn-lg { padding: 1rem 2rem; }

      .btn-spinner {
        display: inline-block;
        width: 16px;
        height: 16px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top-color: white;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-right: 8px;
        vertical-align: middle;
      }
    }

    .help-section {
      text-align: center;
      color: #64748b;
      p { margin: 0; }
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
export class DevisResultatComponent implements OnInit {
  devis = signal<Devis | null>(null);
  isLoading = signal(true);
  isGenerating = signal(false);

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private devisService: DevisService,
    private contratService: ContratService,
    private kycService: KycService
  ) {}

  ngOnInit(): void {
    const devisId = this.route.snapshot.params['id'];
    if (devisId) {
      this.loadDevis(+devisId);
    } else {
      this.router.navigate(['/dashboard/devis']);
    }
  }

  private loadDevis(id: number): void {
    this.devisService.getDevis(id).subscribe({
      next: (devis) => {
        if (!devis.resultatTarif) {
          this.router.navigate(['/devis', id]);
          return;
        }
        this.devis.set(devis);
        this.isLoading.set(false);
      },
      error: () => {
        this.router.navigate(['/dashboard/devis']);
      }
    });
  }

  getFormuleLabel(formule: Formule): string {
    switch (formule) {
      case Formule.ESSENTIELLE: return 'Formule Essentielle';
      case Formule.CONFORT: return 'Formule Confort';
      case Formule.PREMIUM: return 'Formule Premium';
    }
  }

  souscrire(): void {
    const devisData = this.devis();
    if (!devisData) return;

    this.isGenerating.set(true);

    // Vérifier le statut KYC avant de générer le contrat
    this.kycService.getStatus().subscribe({
      next: (status) => {
        if (status.statut !== StatutKyc.VERIFIE) {
          // Rediriger vers la page KYC avec l'ID du devis
          this.isGenerating.set(false);
          this.router.navigate(['/kyc'], { queryParams: { devisId: devisData.id } });
          return;
        }

        // KYC vérifié, on peut générer le contrat
        this.contratService.genererContrat(devisData.id).subscribe({
          next: (contrat) => {
            this.router.navigate(['/contrat', contrat.id]);
          },
          error: (err) => {
            console.error('Erreur lors de la génération du contrat:', err);
            this.isGenerating.set(false);
            if (err.error?.message === 'KYC_REQUIRED') {
              this.router.navigate(['/kyc'], { queryParams: { devisId: devisData.id } });
            } else {
              alert('Une erreur est survenue lors de la génération du contrat.');
            }
          }
        });
      },
      error: () => {
        this.isGenerating.set(false);
        // En cas d'erreur, rediriger vers KYC par précaution
        this.router.navigate(['/kyc'], { queryParams: { devisId: devisData.id } });
      }
    });
  }
}
