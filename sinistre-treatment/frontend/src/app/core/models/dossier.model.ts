export enum StatutDossier {
  A_TRAITER = 'A_TRAITER',
  EN_COURS_ANALYSE = 'EN_COURS_ANALYSE',
  APPROUVE = 'APPROUVE',
  REJETE = 'REJETE'
}

export interface HistoriqueStatut {
  statut: StatutDossier;
  auteur?: string;
  commentaire?: string;
  date: string;
}

export interface DossierSinistre {
  id: number;
  sinistreId: number;
  numeroSinistre: string;
  contratId: number;
  numeroContrat?: string;
  userId: number;
  souscripteurNom?: string;
  souscripteurPrenom?: string;
  type: string;
  dateSinistre: string;
  lieu?: string;
  description?: string;
  montantEstime?: number;
  statut: StatutDossier;
  montantRembourse?: number;
  commentaireDecision?: string;
  decidePar?: string;
  dateDecision?: string;
  dateDeclaration?: string;
  dateReception: string;
  historique: HistoriqueStatut[];
}

export interface DecisionRequest {
  statut: StatutDossier;
  montantRembourse?: number;
  commentaire?: string;
  decidePar: string;
}
