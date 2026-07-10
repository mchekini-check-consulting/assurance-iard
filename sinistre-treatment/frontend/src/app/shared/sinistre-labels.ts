import { StatutDossier } from '../core/models/dossier.model';

export function typeSinistreLabel(type: string): string {
  switch (type) {
    case 'DEGAT_DES_EAUX': return 'Dégât des eaux';
    case 'INCENDIE': return 'Incendie';
    case 'VOL_CAMBRIOLAGE': return 'Vol / Cambriolage';
    case 'BRIS_DE_GLACE': return 'Bris de glace';
    case 'CATASTROPHE_NATURELLE': return 'Catastrophe naturelle';
    case 'AUTRE': return 'Autre';
    default: return type;
  }
}

export function statutDossierLabel(statut: StatutDossier): string {
  switch (statut) {
    case StatutDossier.A_TRAITER: return 'À traiter';
    case StatutDossier.EN_COURS_ANALYSE: return 'En cours d\'analyse';
    case StatutDossier.APPROUVE: return 'Approuvé';
    case StatutDossier.REJETE: return 'Rejeté';
    default: return statut;
  }
}

export function statutDossierClass(statut: StatutDossier): string {
  switch (statut) {
    case StatutDossier.A_TRAITER: return 'badge-a-traiter';
    case StatutDossier.EN_COURS_ANALYSE: return 'badge-en-cours';
    case StatutDossier.APPROUVE: return 'badge-approuve';
    case StatutDossier.REJETE: return 'badge-rejete';
    default: return '';
  }
}
