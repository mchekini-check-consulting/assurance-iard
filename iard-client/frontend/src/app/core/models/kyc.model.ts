export enum StatutKyc {
  NON_VERIFIE = 'NON_VERIFIE',
  EN_COURS = 'EN_COURS',
  VERIFIE = 'VERIFIE',
  REFUSE = 'REFUSE'
}

export enum TypeDocument {
  TITRE_SEJOUR = 'TITRE_SEJOUR',
  RIB = 'RIB'
}

export interface DonneesExtraitesKyc {
  titreSejour_nom?: string;
  titreSejour_prenom?: string;
  titreSejour_numero?: string;
  titreSejour_dateExpiration?: string;
  rib_nom?: string;
  rib_prenom?: string;
  rib_banque?: string;
  rib_iban?: string;
}

export interface KycStatusResponse {
  id?: number;
  statut: StatutKyc;
  donneesExtraites?: DonneesExtraitesKyc;
  dateVerification?: string;
  motifRefus?: string;
  titreSejour_uploaded: boolean;
  rib_uploaded: boolean;
}

export interface TitreSejourExtraction {
  nom?: string;
  prenom?: string;
  numero?: string;
  dateExpiration?: string;
  extractionReussie: boolean;
  erreur?: string;
}

export interface RibExtraction {
  nom?: string;
  prenom?: string;
  banque?: string;
  iban?: string;
  extractionReussie: boolean;
  erreur?: string;
}

export interface KycVerificationResult {
  statut: StatutKyc;
  donneesExtraites?: DonneesExtraitesKyc;
  success: boolean;
  erreurs: string[];
  message: string;
}

export interface DocumentResponse {
  id: number;
  type: TypeDocument;
  nomFichier: string;
  contentType: string;
  createdAt: string;
}
