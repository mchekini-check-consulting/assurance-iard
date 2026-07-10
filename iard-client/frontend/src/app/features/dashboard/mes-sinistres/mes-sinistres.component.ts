import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { SinistreService } from '../../../core/services/sinistre.service';
import {
  Sinistre,
  StatutSinistre,
  STATUT_SINISTRE_LABELS,
  TYPE_SINISTRE_LABELS,
  TypeSinistre
} from '../../../core/models/sinistre.model';

@Component({
  selector: 'app-mes-sinistres',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mes-sinistres.component.html',
  styleUrl: './mes-sinistres.component.scss'
})
export class MesSinistresComponent implements OnInit {
  sinistres = signal<Sinistre[]>([]);
  isLoading = signal(true);

  constructor(private sinistreService: SinistreService) {}

  ngOnInit(): void {
    this.sinistreService.listerSinistres().subscribe({
      next: (sinistres) => {
        this.sinistres.set(sinistres);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  getTypeLabel(type: TypeSinistre): string {
    return TYPE_SINISTRE_LABELS[type] ?? type;
  }

  getStatutLabel(statut: StatutSinistre): string {
    return STATUT_SINISTRE_LABELS[statut] ?? statut;
  }

  getStatutClass(statut: StatutSinistre): string {
    switch (statut) {
      case StatutSinistre.DECLARE: return 'badge-warning';
      case StatutSinistre.EN_COURS_ANALYSE: return 'badge-primary';
      case StatutSinistre.APPROUVE: return 'badge-success';
      case StatutSinistre.REJETE: return 'badge-error';
      default: return 'badge-muted';
    }
  }
}
