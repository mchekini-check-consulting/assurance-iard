import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ContratService } from '../../core/services/contrat.service';
import { Contrat } from '../../core/models/contrat.model';
import { AvenantRequest, Formule } from '../../core/models/devis.model';

interface OptionGarantie {
  code: string;
  libelle: string;
  prix: number;
  selectionnee: boolean;
}

@Component({
  selector: 'app-modifier-garanties',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="modifier-container">
      <header class="modifier-header">
        <a routerLink="/dashboard/contrats" class="back-link">← Mes contrats</a>
        <h1>Modifier mes garanties</h1>
      </header>

      @if (isLoading()) {
        <div class="loading">
          <div class="spinner"></div>
          <p>Chargement du contrat...</p>
        </div>
      } @else if (contrat()) {
        <main class="modifier-content">
          <div class="contrat-banner">
            <div>
              <span class="contrat-numero">{{ contrat()!.numeroContrat }}</span>
              <span class="contrat-adresse">
                {{ contrat()!.donneesRisque.adresse }},
                {{ contrat()!.donneesRisque.codePostal }} {{ contrat()!.donneesRisque.ville }}
              </span>
            </div>
            <div class="prime-actuelle">
              <span class="label">Prime actuelle</span>
              <span class="value">{{ contrat()!.primeTTC | number:'1.2-2' }} €/an</span>
            </div>
          </div>

          <!-- Formule -->
          <section class="form-section">
            <h2>Formule</h2>
            <div class="formules-grid">
              @for (f of formules; track f.value) {
                <label class="formule-card" [class.selected]="formule === f.value">
                  <input type="radio" name="formule" [value]="f.value" [(ngModel)]="formule">
                  <span class="formule-nom">{{ f.label }}</span>
                  <span class="formule-desc">{{ f.description }}</span>
                </label>
              }
            </div>
          </section>

          <!-- Options de garanties -->
          <section class="form-section">
            <h2>Options de garanties</h2>
            <div class="options-grid">
              @for (option of options; track option.code) {
                <label class="option-card" [class.selected]="option.selectionnee">
                  <input type="checkbox" [(ngModel)]="option.selectionnee" [name]="option.code">
                  <span class="option-nom">{{ option.libelle }}</span>
                  <span class="option-prix">+{{ option.prix }} €/an</span>
                </label>
              }
            </div>
          </section>

          <!-- Spécificités du bien -->
          <section class="form-section">
            <h2>Spécificités du bien</h2>
            <div class="specs-grid">
              <label class="spec-toggle">
                <input type="checkbox" [(ngModel)]="piscine" name="piscine">
                <span>🏊 Piscine</span>
              </label>
              <label class="spec-toggle">
                <input type="checkbox" [(ngModel)]="dependances" name="dependances">
                <span>🏚️ Dépendances</span>
              </label>
              <label class="spec-toggle">
                <input type="checkbox" [(ngModel)]="objetsValeur" name="objetsValeur">
                <span>💎 Objets de valeur</span>
              </label>
              <label class="spec-toggle">
                <input type="checkbox" [(ngModel)]="alarme" name="alarme">
                <span>🚨 Alarme</span>
              </label>
              <label class="spec-toggle">
                <input type="checkbox" [(ngModel)]="porteBlindee" name="porteBlindee">
                <span>🚪 Porte blindée</span>
              </label>
            </div>

            @if (objetsValeur) {
              <div class="form-group">
                <label for="valeurObjets">Valeur des objets de valeur (€)</label>
                <input type="number" id="valeurObjets" [(ngModel)]="valeurObjetsValeur"
                       name="valeurObjetsValeur" min="0" step="500">
              </div>
            }

            <div class="form-group">
              <label for="capitalMobilier">Capital mobilier (€)</label>
              <input type="number" id="capitalMobilier" [(ngModel)]="capitalMobilier"
                     name="capitalMobilier" min="0" step="1000">
            </div>
          </section>

          @if (errorMessage()) {
            <div class="error-message">{{ errorMessage() }}</div>
          }

          <div class="actions">
            <a [routerLink]="['/contrat', contrat()!.id]" class="btn btn-outline">Annuler</a>
            <button class="btn btn-primary" [disabled]="isSubmitting()" (click)="genererNouveauDevis()">
              @if (isSubmitting()) {
                <span class="btn-spinner"></span> Calcul du nouveau tarif...
              } @else {
                Recalculer mon tarif →
              }
            </button>
          </div>

          <p class="info-text">
            Un nouveau devis sera généré avec vos garanties modifiées. Après signature
            du nouveau contrat, votre contrat actuel sera automatiquement résilié et remplacé.
          </p>
        </main>
      }
    </div>
  `,
  styles: [`
    .modifier-container {
      min-height: 100vh;
      background: #f8fafc;
    }

    .modifier-header {
      padding: 1.5rem 2rem;
      background: white;
      border-bottom: 1px solid #e2e8f0;

      .back-link {
        color: #64748b;
        text-decoration: none;
        font-size: 0.9rem;
        &:hover { color: #2563eb; }
      }

      h1 { margin: 0.5rem 0 0 0; color: #1e293b; font-size: 1.5rem; }
    }

    .modifier-content {
      max-width: 800px;
      margin: 0 auto;
      padding: 2rem;
    }

    .contrat-banner {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: white;
      border-radius: 12px;
      padding: 1.25rem 1.5rem;
      margin-bottom: 1.5rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      .contrat-numero { display: block; font-weight: 700; color: #1e293b; }
      .contrat-adresse { display: block; color: #64748b; font-size: 0.9rem; margin-top: 0.25rem; }

      .prime-actuelle {
        text-align: right;
        .label { display: block; font-size: 0.8rem; color: #64748b; }
        .value { font-weight: 700; color: #2563eb; font-size: 1.1rem; }
      }
    }

    .form-section {
      background: white;
      border-radius: 12px;
      padding: 1.5rem;
      margin-bottom: 1.5rem;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);

      h2 { margin: 0 0 1rem 0; font-size: 1.1rem; color: #1e293b; }
    }

    .formules-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 1rem;
    }

    .formule-card {
      display: block;
      padding: 1rem;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.15s;

      input { display: none; }

      &.selected {
        border-color: #2563eb;
        background: #eff6ff;
      }

      .formule-nom { display: block; font-weight: 700; color: #1e293b; }
      .formule-desc { display: block; font-size: 0.8rem; color: #64748b; margin-top: 0.25rem; }
    }

    .options-grid, .specs-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 0.75rem;
    }

    .option-card {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      border: 2px solid #e2e8f0;
      border-radius: 10px;
      cursor: pointer;

      &.selected { border-color: #2563eb; background: #eff6ff; }

      .option-nom { flex: 1; color: #1e293b; font-size: 0.9rem; }
      .option-prix { color: #2563eb; font-size: 0.85rem; font-weight: 600; }
    }

    .spec-toggle {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      border: 1px solid #e2e8f0;
      border-radius: 10px;
      cursor: pointer;
      color: #1e293b;
      font-size: 0.9rem;
    }

    .form-group {
      margin-top: 1rem;

      label { display: block; font-size: 0.9rem; color: #64748b; margin-bottom: 0.35rem; }

      input {
        width: 100%;
        max-width: 300px;
        padding: 0.6rem 0.75rem;
        border: 1px solid #e2e8f0;
        border-radius: 8px;
        font-size: 1rem;
      }
    }

    .error-message {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: #b91c1c;
      padding: 0.75rem 1rem;
      border-radius: 8px;
      margin-bottom: 1rem;
    }

    .actions {
      display: flex;
      gap: 1rem;
      justify-content: flex-end;
    }

    .btn {
      padding: 0.875rem 1.5rem;
      border-radius: 8px;
      font-weight: 600;
      text-decoration: none;
      cursor: pointer;
      border: none;
      font-size: 1rem;

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

      .btn-spinner {
        display: inline-block;
        width: 14px;
        height: 14px;
        border: 2px solid rgba(255, 255, 255, 0.3);
        border-top-color: white;
        border-radius: 50%;
        animation: spin 1s linear infinite;
        margin-right: 6px;
        vertical-align: middle;
      }
    }

    .info-text {
      margin-top: 1.5rem;
      padding: 0.75rem 1rem;
      background: #fffbeb;
      border: 1px solid #fde68a;
      border-radius: 8px;
      color: #92400e;
      font-size: 0.85rem;
      line-height: 1.5;
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
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
export class ModifierGarantiesComponent implements OnInit {
  contrat = signal<Contrat | null>(null);
  isLoading = signal(true);
  isSubmitting = signal(false);
  errorMessage = signal<string | null>(null);

  formule: Formule = Formule.ESSENTIELLE;
  piscine = false;
  dependances = false;
  objetsValeur = false;
  alarme = false;
  porteBlindee = false;
  capitalMobilier: number | null = null;
  valeurObjetsValeur: number | null = null;

  formules = [
    { value: Formule.ESSENTIELLE, label: 'Essentielle', description: 'Les garanties indispensables' },
    { value: Formule.CONFORT, label: 'Confort', description: '+ Vol, vandalisme et bris de glace' },
    { value: Formule.PREMIUM, label: 'Premium', description: '+ Dommages électriques, assistance 24h/24' }
  ];

  options: OptionGarantie[] = [
    { code: 'BRIS_GLACE', libelle: 'Bris de glace', prix: 24, selectionnee: false },
    { code: 'VOL_HORS_DOMICILE', libelle: 'Vol hors domicile', prix: 36, selectionnee: false },
    { code: 'JARDIN', libelle: 'Protection jardin', prix: 18, selectionnee: false },
    { code: 'PISCINE_PLUS', libelle: 'Piscine Plus', prix: 45, selectionnee: false },
    { code: 'DOMMAGES_ELECTRIQUES', libelle: 'Dommages électriques', prix: 30, selectionnee: false },
    { code: 'ASSISTANCE_PLUS', libelle: 'Assistance Plus 24/7', prix: 42, selectionnee: false }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private contratService: ContratService
  ) {}

  ngOnInit(): void {
    const contratId = this.route.snapshot.params['id'];
    if (!contratId) {
      this.router.navigate(['/dashboard/contrats']);
      return;
    }

    this.contratService.getContrat(+contratId).subscribe({
      next: (contrat) => {
        if (contrat.statut !== 'ACTIF') {
          this.router.navigate(['/contrat', contrat.id]);
          return;
        }
        this.contrat.set(contrat);
        this.preremplir(contrat);
        this.isLoading.set(false);
      },
      error: () => this.router.navigate(['/dashboard/contrats'])
    });
  }

  private preremplir(contrat: Contrat): void {
    const d = contrat.donneesRisque;
    if (d.formule) this.formule = d.formule;
    this.piscine = !!d.piscine;
    this.dependances = !!d.dependances;
    this.objetsValeur = !!d.objetsValeur;
    this.alarme = !!d.alarme;
    this.porteBlindee = !!d.porteBlindee;
    this.capitalMobilier = d.capitalMobilier ?? null;
    this.valeurObjetsValeur = d.valeurObjetsValeur ?? null;

    const optionsSouscrites = d.optionsGaranties ?? [];
    this.options.forEach(o => o.selectionnee = optionsSouscrites.includes(o.code));
  }

  genererNouveauDevis(): void {
    const c = this.contrat();
    if (!c) return;

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    const request: AvenantRequest = {
      formule: this.formule,
      optionsGaranties: this.options.filter(o => o.selectionnee).map(o => o.code),
      piscine: this.piscine,
      dependances: this.dependances,
      objetsValeur: this.objetsValeur,
      alarme: this.alarme,
      porteBlindee: this.porteBlindee,
      capitalMobilier: this.capitalMobilier ?? undefined,
      valeurObjetsValeur: this.objetsValeur ? (this.valeurObjetsValeur ?? undefined) : undefined
    };

    this.contratService.creerAvenant(c.id, request).subscribe({
      next: (devis) => {
        // Le devis d'avenant est tarifé : on rejoint le parcours habituel
        // (résultat → souscription → signature)
        this.router.navigate(['/devis', devis.id, 'resultat']);
      },
      error: (err) => {
        console.error('Erreur lors de la création de l\'avenant:', err);
        this.errorMessage.set(err.error?.message || 'Une erreur est survenue. Veuillez réessayer.');
        this.isSubmitting.set(false);
      }
    });
  }
}
