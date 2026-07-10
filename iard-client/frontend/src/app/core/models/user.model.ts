export enum Civilite {
  MONSIEUR = 'MONSIEUR',
  MADAME = 'MADAME'
}

export enum Role {
  PARTICULIER = 'PARTICULIER',
  ADMIN = 'ADMIN'
}

export interface User {
  id: number;
  civilite: Civilite;
  prenom: string;
  nom: string;
  email: string;
  role: Role;
  createdAt?: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  civilite: Civilite;
  prenom: string;
  nom: string;
  email: string;
  role: Role;
}

export interface RegisterRequest {
  civilite: Civilite;
  prenom: string;
  nom: string;
  email: string;
  password: string;
  confirmPassword: string;
  acceptCgu: boolean;
}

export interface LoginRequest {
  email: string;
  password: string;
}
