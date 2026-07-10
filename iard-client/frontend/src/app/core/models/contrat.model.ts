import { Formule, Produit, DonneesRisqueHabitation, PersonneAssuree, ResultatTarification } from './devis.model';

export enum StatutContrat {
  EN_ATTENTE = 'EN_ATTENTE',
  ACTIF = 'ACTIF',
  SUSPENDU = 'SUSPENDU',
  RESILIE = 'RESILIE'
}

export enum Periodicite {
  MENSUELLE = 'MENSUELLE',
  TRIMESTRIELLE = 'TRIMESTRIELLE',
  SEMESTRIELLE = 'SEMESTRIELLE',
  ANNUELLE = 'ANNUELLE'
}

export interface Contrat {
  id: number;
  numeroContrat: string;
  devisId: number;
  produit: Produit;
  formule: Formule;
  garanties: ResultatTarification;
  primeHT: number;
  taxes: number;
  primeTTC: number;
  periodicite: Periodicite;
  statut: StatutContrat;
  dateSignature?: string;
  signatureId?: string;
  pdfUrl: string;
  createdAt: string;
  donneesRisque: DonneesRisqueHabitation;
  assure: PersonneAssuree;
  souscripteurNom: string;
  souscripteurPrenom: string;
  souscripteurEmail: string;
}

export interface SignatureRequest {
  code: string;
}

export interface ContratStats {
  actifs: number;
  enAttente: number;
  total: number;
}
