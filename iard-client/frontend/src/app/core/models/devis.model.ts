export enum Produit {
  HABITATION = 'HABITATION',
  AUTO = 'AUTO',
  RC_PRO = 'RC_PRO'
}

export enum StatutDevis {
  BROUILLON = 'BROUILLON',
  DEVIS = 'DEVIS',
  PROPOSITION = 'PROPOSITION',
  ACCEPTE = 'ACCEPTE',
  REFUSE = 'REFUSE',
  EXPIRE = 'EXPIRE',
  TRANSFORME = 'TRANSFORME'
}

export enum TypeBien {
  APPARTEMENT = 'APPARTEMENT',
  MAISON = 'MAISON'
}

export enum TypeResidence {
  PRINCIPALE = 'PRINCIPALE',
  SECONDAIRE = 'SECONDAIRE'
}

export enum StatutOccupation {
  PROPRIETAIRE_OCCUPANT = 'PROPRIETAIRE_OCCUPANT',
  LOCATAIRE = 'LOCATAIRE',
  PROPRIETAIRE_NON_OCCUPANT = 'PROPRIETAIRE_NON_OCCUPANT'
}

export enum Formule {
  ESSENTIELLE = 'ESSENTIELLE',
  CONFORT = 'CONFORT',
  PREMIUM = 'PREMIUM'
}

export interface DonneesRisqueHabitation {
  typeBien?: TypeBien;
  typeResidence?: TypeResidence;
  adresse?: string;
  codePostal?: string;
  ville?: string;
  surfaceHabitable?: number;
  nombrePieces?: number;
  etage?: number;
  anneeConstruction?: number;
  statutOccupation?: StatutOccupation;
  alarme?: boolean;
  porteBlindee?: boolean;
  dependances?: boolean;
  surfaceDependances?: number;
  piscine?: boolean;
  capitalMobilier?: number;
  objetsValeur?: boolean;
  valeurObjetsValeur?: number;
  nombreSinistres36Mois?: number;
  formule?: Formule;
  optionsGaranties?: string[];
}

export interface PersonneAssuree {
  civilite?: string;
  prenom?: string;
  nom?: string;
  email?: string;
  telephone?: string;
  adresse?: string;
  codePostal?: string;
  ville?: string;
}

export interface GarantieDetail {
  code: string;
  libelle: string;
  plafond?: number;
  franchise?: number;
  incluse: boolean;
  primeSupplementaire?: number;
}

export interface ResultatTarification {
  formule: Formule;
  primeHT: number;
  taxes: number;
  primeTTC: number;
  primeMensuelle: number;
  garantiesIncluses: GarantieDetail[];
  garantiesOptionnelles: GarantieDetail[];
}

export interface Devis {
  id: number;
  produit: Produit;
  statut: StatutDevis;
  etapeCourante: number;
  donneesRisque?: DonneesRisqueHabitation;
  assure?: PersonneAssuree;
  resultatTarif?: ResultatTarification;
  createdAt: string;
  updatedAt: string;
  adresseResume?: string;
  formuleResume?: string;
  primeTTCResume?: string;
}

export interface DevisRequest {
  etapeCourante?: number;
  typeBien?: TypeBien;
  typeResidence?: TypeResidence;
  adresse?: string;
  codePostal?: string;
  ville?: string;
  surfaceHabitable?: number;
  nombrePieces?: number;
  etage?: number;
  anneeConstruction?: number;
  statutOccupation?: StatutOccupation;
  alarme?: boolean;
  porteBlindee?: boolean;
  dependances?: boolean;
  surfaceDependances?: number;
  piscine?: boolean;
  capitalMobilier?: number;
  objetsValeur?: boolean;
  valeurObjetsValeur?: number;
  nombreSinistres36Mois?: number;
  formule?: Formule;
  optionsGaranties?: string[];
  souscripteurEstAssure?: boolean;
  assure?: PersonneAssuree;
}

export interface DevisStats {
  brouillons: number;
  devis: number;
}
