import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SinistreService } from '../../../core/services/sinistre.service';
import {
  Sinistre,
  StatutSinistre,
  STATUT_SINISTRE_LABELS,
  TYPE_SINISTRE_LABELS,
  TypeSinistre
} from '../../../core/models/sinistre.model';

@Component({
  selector: 'app-sinistre-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './sinistre-detail.component.html',
  styleUrl: './sinistre-detail.component.scss'
})
export class SinistreDetailComponent implements OnInit {
  sinistre = signal<Sinistre | null>(null);
  isLoading = signal(true);

  constructor(
    private route: ActivatedRoute,
    private sinistreService: SinistreService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.sinistreService.getSinistre(id).subscribe({
      next: (sinistre) => {
        this.sinistre.set(sinistre);
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

  getPieceJointeUrl(pieceId: number): string {
    const s = this.sinistre();
    return s ? this.sinistreService.getPieceJointeUrl(s.id, pieceId) : '';
  }
}
