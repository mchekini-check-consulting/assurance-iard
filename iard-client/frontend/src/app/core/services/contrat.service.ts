import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Contrat, SignatureRequest, ContratStats, StatutContrat } from '../models/contrat.model';

@Injectable({
  providedIn: 'root'
})
export class ContratService {
  private readonly API_URL = '/api/contrats';

  constructor(private http: HttpClient) {}

  genererContrat(devisId: number): Observable<Contrat> {
    return this.http.post<Contrat>(`${this.API_URL}/generer/${devisId}`, {});
  }

  listerContrats(statut?: StatutContrat): Observable<Contrat[]> {
    if (statut) {
      return this.http.get<Contrat[]>(this.API_URL, { params: { statut } });
    }
    return this.http.get<Contrat[]>(this.API_URL);
  }

  getContrat(id: number): Observable<Contrat> {
    return this.http.get<Contrat>(`${this.API_URL}/${id}`);
  }

  signerContrat(id: number, code: string): Observable<Contrat> {
    const request: SignatureRequest = { code };
    return this.http.post<Contrat>(`${this.API_URL}/${id}/signer`, request);
  }

  getPdfUrl(id: number): string {
    return `${this.API_URL}/${id}/pdf`;
  }

  getPdfBlob(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/pdf`, { responseType: 'blob' });
  }

  getStats(): Observable<ContratStats> {
    return this.http.get<ContratStats>(`${this.API_URL}/stats`);
  }
}
