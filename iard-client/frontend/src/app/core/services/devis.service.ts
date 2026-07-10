import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Devis, DevisRequest, DevisStats } from '../models/devis.model';

@Injectable({
  providedIn: 'root'
})
export class DevisService {
  private readonly API_URL = '/api/devis';

  constructor(private http: HttpClient) {}

  creerDevis(): Observable<Devis> {
    return this.http.post<Devis>(this.API_URL, {});
  }

  listerDevis(): Observable<Devis[]> {
    return this.http.get<Devis[]>(this.API_URL);
  }

  getDevis(id: number): Observable<Devis> {
    return this.http.get<Devis>(`${this.API_URL}/${id}`);
  }

  sauvegarderEtape(id: number, request: DevisRequest): Observable<Devis> {
    return this.http.put<Devis>(`${this.API_URL}/${id}`, request);
  }

  calculerTarif(id: number): Observable<Devis> {
    return this.http.post<Devis>(`${this.API_URL}/${id}/tarifier`, {});
  }

  supprimerDevis(id: number): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  getStats(): Observable<DevisStats> {
    return this.http.get<DevisStats>(`${this.API_URL}/stats`);
  }
}
