import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { DossierService } from '../../core/services/dossier.service';
import { DossierSinistre, StatutDossier } from '../../core/models/dossier.model';
import { statutDossierClass, statutDossierLabel, typeSinistreLabel } from '../../shared/sinistre-labels';

@Component({
  selector: 'app-dossier-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <a routerLink="/" class="back">‹ Retour à la liste</a>

    @if (isLoading()) {
      <div class="card empty">Chargement…</div>
    } @else if (!dossier()) {
      <div class="card empty">Dossier introuvable.</div>
    } @else {
      @if (dossier(); as d) {
        <div class="header">
          <div>
            <h1>{{ d.numeroSinistre }}</h1>
            <p class="subtitle">Déclaré le {{ d.dateDeclaration | date:'dd/MM/yyyy HH:mm' }} — reçu le {{ d.dateReception | date:'dd/MM/yyyy HH:mm' }}</p>
          </div>
          <span class="badge" [class]="'badge ' + statutClass(d.statut)">{{ statutLabel(d.statut) }}</span>
        </div>

        @if (message()) {
          <div class="alert" [class.alert-error]="isError()">{{ message() }}</div>
        }

        <div class="grid">
          <div class="card">
            <h2>Sinistre</h2>
            <dl>
              <div><dt>Type</dt><dd>{{ typeLabel(d.type) }}</dd></div>
              <div><dt>Date du sinistre</dt><dd>{{ d.dateSinistre | date:'dd/MM/yyyy' }}</dd></div>
              <div><dt>Lieu</dt><dd>{{ d.lieu || '—' }}</dd></div>
              <div><dt>Montant estimé</dt><dd>{{ d.montantEstime != null ? (d.montantEstime | number:'1.2-2') + ' €' : '—' }}</dd></div>
            </dl>
            <h3>Description</h3>
            <p class="description">{{ d.description }}</p>
          </div>

          <div class="card">
            <h2>Contrat &amp; souscripteur</h2>
            <dl>
              <div><dt>Contrat</dt><dd>{{ d.numeroContrat || d.contratId }}</dd></div>
              <div><dt>Souscripteur</dt><dd>{{ d.souscripteurPrenom }} {{ d.souscripteurNom }}</dd></div>
              <div><dt>Référence souscripteur</dt><dd>#{{ d.userId }}</dd></div>
            </dl>

            @if (d.statut === 'APPROUVE' || d.statut === 'REJETE') {
              <h2 class="mt">Décision</h2>
              <dl>
                <div><dt>Montant remboursé</dt><dd>{{ d.montantRembourse != null ? (d.montantRembourse | number:'1.2-2') + ' €' : '—' }}</dd></div>
                <div><dt>Motif / commentaire</dt><dd>{{ d.commentaireDecision || '—' }}</dd></div>
                <div><dt>Décidé par</dt><dd>{{ d.decidePar }}</dd></div>
                <div><dt>Date de décision</dt><dd>{{ d.dateDecision | date:'dd/MM/yyyy HH:mm' }}</dd></div>
              </dl>
            }
          </div>
        </div>

        <!-- Instruction du dossier -->
        @if (d.statut === 'A_TRAITER' || d.statut === 'EN_COURS_ANALYSE') {
          <div class="card">
            <h2>Instruction du dossier</h2>

            <div class="form-row">
              <label for="decidePar">Gestionnaire</label>
              <input id="decidePar" type="text" [(ngModel)]="decidePar" placeholder="Nom du gestionnaire">
            </div>

            @if (d.statut === 'A_TRAITER') {
              <p class="hint">Le dossier doit d'abord passer en analyse avant de pouvoir statuer.</p>
              <button class="btn btn-primary" [disabled]="isSubmitting()" (click)="demarrerAnalyse()">
                Passer en cours d'analyse
              </button>
            } @else {
              <div class="form-row">
                <label>Décision</label>
                <div class="radio-group">
                  <label class="radio">
                    <input type="radio" name="decision" value="APPROUVE" [(ngModel)]="decision">
                    Approuver
                  </label>
                  <label class="radio">
                    <input type="radio" name="decision" value="REJETE" [(ngModel)]="decision">
                    Rejeter
                  </label>
                </div>
              </div>

              @if (decision === 'APPROUVE') {
                <div class="form-row">
                  <label for="montant">Montant du remboursement pris en charge (€) *</label>
                  <input id="montant" type="number" min="0" step="0.01" [(ngModel)]="montantRembourse">
                </div>
              }
              @if (decision === 'REJETE') {
                <p class="hint">Montant de remboursement fixé à 0 € (non modifiable en cas de rejet).</p>
              }

              <div class="form-row">
                <label for="commentaire">Commentaire / motif de décision {{ decision === 'REJETE' ? '(obligatoire)' : '' }}</label>
                <textarea id="commentaire" rows="3" [(ngModel)]="commentaire"
                          placeholder="Motif de la décision"></textarea>
              </div>

              <button class="btn btn-primary" [disabled]="isSubmitting() || !decision" (click)="statuer()">
                Enregistrer la décision
              </button>
            }
          </div>
        }

        <!-- Historique -->
        <div class="card">
          <h2>Historique des statuts</h2>
          <ul class="timeline">
            @for (h of d.historique; track $index) {
              <li>
                <span class="dot" [class]="'dot ' + statutClass(h.statut)"></span>
                <div>
                  <div class="tl-head">
                    <strong>{{ statutLabel(h.statut) }}</strong>
                    <span class="tl-date">{{ h.date | date:'dd/MM/yyyy HH:mm' }}</span>
                  </div>
                  @if (h.auteur) { <div class="tl-meta">Par {{ h.auteur }}</div> }
                  @if (h.commentaire) { <div class="tl-meta">{{ h.commentaire }}</div> }
                </div>
              </li>
            }
          </ul>
        </div>
      }
    }
  `,
  styles: [`
    .back {
      display: inline-block;
      margin-bottom: 16px;
      color: #4b5563;
      font-size: 14px;
    }

    .back:hover { color: #111827; }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
      gap: 16px;
    }

    h1 { font-size: 24px; color: #111827; }

    .subtitle { color: #6b7280; margin-top: 4px; font-size: 13px; }

    .grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 20px;
      margin-bottom: 20px;
    }

    @media (max-width: 800px) {
      .grid { grid-template-columns: 1fr; }
    }

    .card {
      background: #fff;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      margin-bottom: 20px;
    }

    .card.empty { text-align: center; color: #6b7280; padding: 48px; }

    h2 {
      font-size: 15px;
      text-transform: uppercase;
      letter-spacing: 0.4px;
      color: #6b7280;
      margin-bottom: 16px;
    }

    h2.mt { margin-top: 24px; }

    h3 {
      font-size: 13px;
      text-transform: uppercase;
      color: #6b7280;
      margin: 16px 0 8px;
    }

    dl div {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      padding: 7px 0;
      border-bottom: 1px solid #f3f4f6;
    }

    dt { color: #6b7280; font-size: 14px; }

    dd { font-weight: 600; font-size: 14px; text-align: right; }

    .description { color: #374151; line-height: 1.6; white-space: pre-wrap; }

    .form-row { margin-bottom: 16px; display: flex; flex-direction: column; gap: 6px; }

    label { font-size: 14px; font-weight: 600; color: #374151; }

    input, textarea {
      border: 1px solid #d1d5db;
      border-radius: 8px;
      padding: 10px 12px;
      font-size: 14px;
      font-family: inherit;
      max-width: 480px;
    }

    .radio-group { display: flex; gap: 24px; }

    .radio { display: flex; align-items: center; gap: 8px; font-weight: 500; cursor: pointer; }

    .hint { color: #6b7280; font-size: 13px; margin-bottom: 14px; }

    .btn {
      border: none;
      border-radius: 8px;
      padding: 11px 22px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
    }

    .btn-primary { background: #111827; color: #fff; }

    .btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

    .alert {
      background: #d1fae5;
      color: #065f46;
      border-radius: 8px;
      padding: 12px 16px;
      margin-bottom: 16px;
      font-size: 14px;
    }

    .alert-error { background: #fee2e2; color: #991b1b; }

    .timeline { list-style: none; }

    .timeline li {
      display: flex;
      gap: 12px;
      padding: 10px 0;
      border-bottom: 1px solid #f3f4f6;
    }

    .timeline li:last-child { border-bottom: none; }

    .dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
      margin-top: 5px;
      flex-shrink: 0;
      padding: 0;
    }

    .tl-head { display: flex; gap: 12px; align-items: baseline; }

    .tl-date { color: #9ca3af; font-size: 13px; }

    .tl-meta { color: #6b7280; font-size: 13px; margin-top: 2px; }
  `]
})
export class DossierDetailComponent implements OnInit {
  dossier = signal<DossierSinistre | null>(null);
  isLoading = signal(true);
  isSubmitting = signal(false);
  message = signal('');
  isError = signal(false);

  decidePar = 'Gestionnaire sinistres';
  decision: 'APPROUVE' | 'REJETE' | '' = '';
  montantRembourse: number | null = null;
  commentaire = '';

  constructor(
    private route: ActivatedRoute,
    private dossierService: DossierService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.dossierService.getDossier(id).subscribe({
      next: (dossier) => {
        this.dossier.set(dossier);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  demarrerAnalyse(): void {
    this.envoyerDecision({
      statut: StatutDossier.EN_COURS_ANALYSE,
      decidePar: this.decidePar
    });
  }

  statuer(): void {
    if (!this.decision) return;
    this.envoyerDecision({
      statut: this.decision as StatutDossier,
      montantRembourse: this.decision === 'APPROUVE' ? (this.montantRembourse ?? undefined) : undefined,
      commentaire: this.commentaire || undefined,
      decidePar: this.decidePar
    });
  }

  private envoyerDecision(request: { statut: StatutDossier; montantRembourse?: number; commentaire?: string; decidePar: string }): void {
    const d = this.dossier();
    if (!d) return;
    this.isSubmitting.set(true);
    this.message.set('');
    this.dossierService.decider(d.id, request).subscribe({
      next: (dossier) => {
        this.dossier.set(dossier);
        this.isSubmitting.set(false);
        this.isError.set(false);
        this.message.set('Décision enregistrée. La plateforme de souscription sera synchronisée via Kafka.');
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.isError.set(true);
        this.message.set(err?.error?.message || Object.values(err?.error || {}).join(', ') || 'Erreur lors de l\'enregistrement de la décision.');
      }
    });
  }

  typeLabel = typeSinistreLabel;
  statutLabel = statutDossierLabel;
  statutClass = statutDossierClass;
}
