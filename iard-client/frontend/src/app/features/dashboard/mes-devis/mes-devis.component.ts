import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { DevisService } from '../../../core/services/devis.service';
import { Devis, StatutDevis } from '../../../core/models/devis.model';

@Component({
  selector: 'app-mes-devis',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mes-devis.component.html',
  styleUrl: './mes-devis.component.scss'
})
export class MesDevisComponent implements OnInit {
  devis = signal<Devis[]>([]);
  isLoading = signal(true);
  filter = signal<'all' | 'brouillon' | 'devis'>('all');

  constructor(private devisService: DevisService) {}

  ngOnInit(): void {
    this.loadDevis();
  }

  loadDevis(): void {
    this.isLoading.set(true);
    this.devisService.listerDevis().subscribe({
      next: (devis) => {
        this.devis.set(devis);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  get filteredDevis(): Devis[] {
    const all = this.devis();
    switch (this.filter()) {
      case 'brouillon':
        return all.filter(d => d.statut === StatutDevis.BROUILLON);
      case 'devis':
        return all.filter(d => d.statut === StatutDevis.DEVIS);
      default:
        return all;
    }
  }

  setFilter(filter: 'all' | 'brouillon' | 'devis'): void {
    this.filter.set(filter);
  }

  getStatutLabel(statut: StatutDevis): string {
    switch (statut) {
      case StatutDevis.BROUILLON: return 'Brouillon';
      case StatutDevis.DEVIS: return 'Devis';
      case StatutDevis.PROPOSITION: return 'Proposition';
      case StatutDevis.ACCEPTE: return 'Accepté';
      case StatutDevis.REFUSE: return 'Refusé';
      case StatutDevis.EXPIRE: return 'Expiré';
      case StatutDevis.TRANSFORME: return 'Transformé';
      default: return statut;
    }
  }

  getStatutClass(statut: StatutDevis): string {
    switch (statut) {
      case StatutDevis.BROUILLON: return 'badge-warning';
      case StatutDevis.DEVIS: return 'badge-success';
      case StatutDevis.PROPOSITION: return 'badge-primary';
      case StatutDevis.ACCEPTE: return 'badge-primary';
      case StatutDevis.REFUSE: return 'badge-error';
      case StatutDevis.EXPIRE: return 'badge-muted';
      case StatutDevis.TRANSFORME: return 'badge-success';
      default: return '';
    }
  }

  supprimerDevis(id: number, event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    if (confirm('Êtes-vous sûr de vouloir supprimer ce devis ?')) {
      this.devisService.supprimerDevis(id).subscribe({
        next: () => {
          this.devis.update(list => list.filter(d => d.id !== id));
        }
      });
    }
  }

  formatDate(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }
}
