import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SinistreService } from '../../../core/services/sinistre.service';
import { ContratService } from '../../../core/services/contrat.service';
import { Contrat, StatutContrat } from '../../../core/models/contrat.model';
import { Produit } from '../../../core/models/devis.model';
import {
  DeclarationSinistreRequest,
  Sinistre,
  TYPE_SINISTRE_LABELS,
  TypeSinistre
} from '../../../core/models/sinistre.model';

const MAX_FICHIERS = 5;
const MAX_TAILLE_FICHIER = 5 * 1024 * 1024; // 5 Mo
const TYPES_AUTORISES = ['image/jpeg', 'image/png', 'application/pdf'];

@Component({
  selector: 'app-declarer-sinistre',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './declarer-sinistre.component.html',
  styleUrl: './declarer-sinistre.component.scss'
})
export class DeclarerSinistreComponent implements OnInit {
  contrats = signal<Contrat[]>([]);
  isLoading = signal(true);
  isSubmitting = signal(false);
  errorMessage = signal('');
  sinistreDeclare = signal<Sinistre | null>(null);

  typesSinistre = Object.values(TypeSinistre);
  typeLabels = TYPE_SINISTRE_LABELS;
  today = new Date().toISOString().split('T')[0];

  contratId: number | null = null;
  type: TypeSinistre | '' = '';
  dateSinistre = '';
  lieu = '';
  description = '';
  montantEstime: number | null = null;
  fichiers: File[] = [];

  constructor(
    private sinistreService: SinistreService,
    private contratService: ContratService
  ) {}

  ngOnInit(): void {
    // Seuls les contrats habitation actifs sont éligibles à une déclaration
    this.contratService.listerContrats(StatutContrat.ACTIF).subscribe({
      next: (contrats) => {
        this.contrats.set(contrats.filter(c => c.produit === Produit.HABITATION));
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  get contratSelectionne(): Contrat | undefined {
    return this.contrats().find(c => c.id === Number(this.contratId));
  }

  /** Date d'effet du contrat sélectionné : borne minimale de la date du sinistre */
  get dateEffet(): string {
    const contrat = this.contratSelectionne;
    if (!contrat) return '';
    const date = contrat.dateSignature ?? contrat.createdAt;
    return date ? date.split('T')[0] : '';
  }

  onContratChange(): void {
    const contrat = this.contratSelectionne;
    if (contrat?.donneesRisque) {
      // Lieu pré-rempli avec l'adresse du logement assuré (modifiable)
      const { adresse, codePostal, ville } = contrat.donneesRisque;
      this.lieu = [adresse, codePostal, ville].filter(Boolean).join(', ');
    }
  }

  onFichiersChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const nouveaux = Array.from(input.files ?? []);
    this.errorMessage.set('');

    if (nouveaux.length > MAX_FICHIERS) {
      this.errorMessage.set(`Maximum ${MAX_FICHIERS} pièces jointes autorisées.`);
      input.value = '';
      return;
    }
    for (const fichier of nouveaux) {
      if (!TYPES_AUTORISES.includes(fichier.type)) {
        this.errorMessage.set(`Format non autorisé pour ${fichier.name} (jpg, png ou pdf uniquement).`);
        input.value = '';
        return;
      }
      if (fichier.size > MAX_TAILLE_FICHIER) {
        this.errorMessage.set(`Le fichier ${fichier.name} dépasse 5 Mo.`);
        input.value = '';
        return;
      }
    }
    this.fichiers = nouveaux;
  }

  retirerFichier(index: number): void {
    this.fichiers = this.fichiers.filter((_, i) => i !== index);
  }

  get descriptionValide(): boolean {
    return this.description.trim().length >= 50;
  }

  get formulaireValide(): boolean {
    return !!this.contratId
      && !!this.type
      && !!this.dateSinistre
      && this.dateSinistre <= this.today
      && (!this.dateEffet || this.dateSinistre >= this.dateEffet)
      && !!this.lieu.trim()
      && this.descriptionValide
      && (this.montantEstime == null || this.montantEstime >= 0);
  }

  soumettre(): void {
    if (!this.formulaireValide || this.isSubmitting()) return;

    const declaration: DeclarationSinistreRequest = {
      contratId: Number(this.contratId),
      type: this.type as TypeSinistre,
      dateSinistre: this.dateSinistre,
      lieu: this.lieu.trim(),
      description: this.description.trim(),
      montantEstime: this.montantEstime ?? undefined
    };

    this.isSubmitting.set(true);
    this.errorMessage.set('');
    this.sinistreService.declarerSinistre(declaration, this.fichiers).subscribe({
      next: (sinistre) => {
        this.isSubmitting.set(false);
        this.sinistreDeclare.set(sinistre);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const messages = err?.error ? Object.values(err.error).join(' — ') : '';
        this.errorMessage.set(messages || 'Erreur lors de la déclaration du sinistre.');
      }
    });
  }
}
