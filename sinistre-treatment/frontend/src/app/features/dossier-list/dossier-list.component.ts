import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DossierService } from '../../core/services/dossier.service';
import { DossierSinistre, StatutDossier } from '../../core/models/dossier.model';
import { statutDossierClass, statutDossierLabel, typeSinistreLabel } from '../../shared/sinistre-labels';

@Component({
  selector: 'app-dossier-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="header">
      <div>
        <h1>Dossiers sinistres</h1>
        <p class="subtitle">{{ filteredDossiers().length }} dossier(s)</p>
      </div>
      <div class="filters">
        @for (f of filtres; track f.value) {
          <button
            class="filter-btn"
            [class.active]="filtre() === f.value"
            (click)="filtre.set(f.value)">
            {{ f.label }}
          </button>
        }
      </div>
    </div>

    @if (isLoading()) {
      <div class="empty">Chargement…</div>
    } @else if (filteredDossiers().length === 0) {
      <div class="empty">Aucun dossier sinistre.</div>
    } @else {
      <div class="table-card">
        <table>
          <thead>
            <tr>
              <th>Numéro</th>
              <th>Type</th>
              <th>Date sinistre</th>
              <th>Contrat</th>
              <th>Souscripteur</th>
              <th>Montant estimé</th>
              <th>Statut</th>
              <th>Reçu le</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            @for (dossier of filteredDossiers(); track dossier.id) {
              <tr [routerLink]="['/dossiers', dossier.id]">
                <td class="numero">{{ dossier.numeroSinistre }}</td>
                <td>{{ typeLabel(dossier.type) }}</td>
                <td>{{ dossier.dateSinistre | date:'dd/MM/yyyy' }}</td>
                <td>{{ dossier.numeroContrat || dossier.contratId }}</td>
                <td>{{ dossier.souscripteurPrenom }} {{ dossier.souscripteurNom }}</td>
                <td>{{ dossier.montantEstime != null ? (dossier.montantEstime | number:'1.2-2') + ' €' : '—' }}</td>
                <td><span class="badge" [class]="'badge ' + statutClass(dossier.statut)">{{ statutLabel(dossier.statut) }}</span></td>
                <td>{{ dossier.dateReception | date:'dd/MM/yyyy HH:mm' }}</td>
                <td class="chevron">›</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
  styles: [`
    .header {
      display: flex;
      justify-content: space-between;
      align-items: flex-end;
      margin-bottom: 20px;
      gap: 16px;
      flex-wrap: wrap;
    }

    h1 {
      font-size: 24px;
      color: #111827;
    }

    .subtitle {
      color: #6b7280;
      margin-top: 4px;
      font-size: 13px;
    }

    .filters {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .filter-btn {
      border: 1px solid #d1d5db;
      background: #fff;
      border-radius: 999px;
      padding: 6px 14px;
      font-size: 13px;
      cursor: pointer;
      color: #374151;
    }

    .filter-btn.active {
      background: #111827;
      border-color: #111827;
      color: #fff;
    }

    .table-card {
      background: #fff;
      border-radius: 12px;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
      overflow-x: auto;
    }

    table {
      width: 100%;
      border-collapse: collapse;
    }

    th {
      text-align: left;
      font-size: 12px;
      text-transform: uppercase;
      letter-spacing: 0.4px;
      color: #6b7280;
      padding: 14px 16px;
      border-bottom: 1px solid #e5e7eb;
    }

    td {
      padding: 14px 16px;
      border-bottom: 1px solid #f3f4f6;
      font-size: 14px;
    }

    tbody tr {
      cursor: pointer;
      transition: background 0.12s;
    }

    tbody tr:hover {
      background: #f9fafb;
    }

    .numero {
      font-weight: 600;
      color: #111827;
    }

    .chevron {
      color: #9ca3af;
      font-size: 18px;
    }

    .empty {
      background: #fff;
      border-radius: 12px;
      padding: 48px;
      text-align: center;
      color: #6b7280;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    }
  `]
})
export class DossierListComponent implements OnInit {
  dossiers = signal<DossierSinistre[]>([]);
  isLoading = signal(true);
  filtre = signal<StatutDossier | 'ALL'>('ALL');

  filtres: { value: StatutDossier | 'ALL'; label: string }[] = [
    { value: 'ALL', label: 'Tous' },
    { value: StatutDossier.A_TRAITER, label: 'À traiter' },
    { value: StatutDossier.EN_COURS_ANALYSE, label: 'En cours' },
    { value: StatutDossier.APPROUVE, label: 'Approuvés' },
    { value: StatutDossier.REJETE, label: 'Rejetés' }
  ];

  constructor(private dossierService: DossierService) {}

  ngOnInit(): void {
    this.dossierService.listerDossiers().subscribe({
      next: (dossiers) => {
        this.dossiers.set(dossiers);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  filteredDossiers(): DossierSinistre[] {
    const f = this.filtre();
    return f === 'ALL' ? this.dossiers() : this.dossiers().filter(d => d.statut === f);
  }

  typeLabel = typeSinistreLabel;
  statutLabel = statutDossierLabel;
  statutClass = statutDossierClass;
}
