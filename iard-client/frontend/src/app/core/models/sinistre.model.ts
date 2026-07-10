export enum TypeSinistre {
  DEGAT_DES_EAUX = 'DEGAT_DES_EAUX',
  INCENDIE = 'INCENDIE',
  VOL_CAMBRIOLAGE = 'VOL_CAMBRIOLAGE',
  BRIS_DE_GLACE = 'BRIS_DE_GLACE',
  CATASTROPHE_NATURELLE = 'CATASTROPHE_NATURELLE',
  AUTRE = 'AUTRE'
}

export enum StatutSinistre {
  DECLARE = 'DECLARE',
  EN_COURS_ANALYSE = 'EN_COURS_ANALYSE',
  APPROUVE = 'APPROUVE',
  REJETE = 'REJETE'
}

export interface PieceJointe {
  id: number;
  nomFichier: string;
}

export interface EtapeStatut {
  statut: StatutSinistre;
  date: string;
}

export interface Sinistre {
  id: number;
  numeroSinistre: string;
  contratId: number;
  numeroContrat: string;
  type: TypeSinistre;
  dateSinistre: string;
  lieu: string;
  description: string;
  montantEstime?: number;
  statut: StatutSinistre;
  montantRembourse?: number;
  commentaireDecision?: string;
  dateDecision?: string;
  createdAt: string;
  piecesJointes: PieceJointe[];
  timeline: EtapeStatut[];
}

export interface DeclarationSinistreRequest {
  contratId: number;
  type: TypeSinistre;
  dateSinistre: string;
  lieu: string;
  description: string;
  montantEstime?: number;
}

export const TYPE_SINISTRE_LABELS: Record<TypeSinistre, string> = {
  [TypeSinistre.DEGAT_DES_EAUX]: 'Dégât des eaux',
  [TypeSinistre.INCENDIE]: 'Incendie',
  [TypeSinistre.VOL_CAMBRIOLAGE]: 'Vol / Cambriolage',
  [TypeSinistre.BRIS_DE_GLACE]: 'Bris de glace',
  [TypeSinistre.CATASTROPHE_NATURELLE]: 'Catastrophe naturelle',
  [TypeSinistre.AUTRE]: 'Autre'
};

export const STATUT_SINISTRE_LABELS: Record<StatutSinistre, string> = {
  [StatutSinistre.DECLARE]: 'Déclaré',
  [StatutSinistre.EN_COURS_ANALYSE]: 'En cours d\'analyse',
  [StatutSinistre.APPROUVE]: 'Approuvé',
  [StatutSinistre.REJETE]: 'Rejeté'
};
