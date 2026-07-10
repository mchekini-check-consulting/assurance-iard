import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ContratService } from '../../../core/services/contrat.service';
import { Contrat, StatutContrat } from '../../../core/models/contrat.model';
import { Formule } from '../../../core/models/devis.model';

@Component({
  selector: 'app-mes-contrats',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mes-contrats.component.html',
  styleUrl: './mes-contrats.component.scss'
})
export class MesContratsComponent implements OnInit {
  contrats = signal<Contrat[]>([]);
  isLoading = signal(true);
  filter = signal<'all' | 'en_attente' | 'actif'>('all');

  constructor(private contratService: ContratService) {}

  ngOnInit(): void {
    this.loadContrats();
  }

  loadContrats(): void {
    this.isLoading.set(true);
    this.contratService.listerContrats().subscribe({
      next: (contrats) => {
        this.contrats.set(contrats);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
      }
    });
  }

  get filteredContrats(): Contrat[] {
    const all = this.contrats();
    switch (this.filter()) {
      case 'en_attente':
        return all.filter(c => c.statut === StatutContrat.EN_ATTENTE);
      case 'actif':
        return all.filter(c => c.statut === StatutContrat.ACTIF);
      default:
        return all;
    }
  }

  setFilter(filter: 'all' | 'en_attente' | 'actif'): void {
    this.filter.set(filter);
  }

  getStatutLabel(contrat: Contrat): string {
    switch (contrat.statut) {
      case StatutContrat.EN_ATTENTE:
        // Signé mais pas encore activé par le premier prélèvement
        return contrat.dateSignature
          ? 'Signé — en attente de prélèvement'
          : 'En attente de signature';
      case StatutContrat.ACTIF: return 'Actif';
      case StatutContrat.SUSPENDU: return 'Suspendu';
      case StatutContrat.RESILIE: return 'Résilié';
      default: return contrat.statut;
    }
  }

  getStatutClass(statut: StatutContrat): string {
    switch (statut) {
      case StatutContrat.EN_ATTENTE: return 'badge-warning';
      case StatutContrat.ACTIF: return 'badge-success';
      case StatutContrat.SUSPENDU: return 'badge-muted';
      case StatutContrat.RESILIE: return 'badge-error';
      default: return '';
    }
  }

  getFormuleLabel(formule: Formule): string {
    switch (formule) {
      case Formule.ESSENTIELLE: return 'Essentielle';
      case Formule.CONFORT: return 'Confort';
      case Formule.PREMIUM: return 'Premium';
      default: return formule;
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

  formatDateTime(dateStr: string): string {
    const date = new Date(dateStr);
    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
