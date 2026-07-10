import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DeclarationSinistreRequest, Sinistre } from '../models/sinistre.model';

@Injectable({
  providedIn: 'root'
})
export class SinistreService {
  private readonly API_URL = '/api/sinistres';

  constructor(private http: HttpClient) {}

  declarerSinistre(declaration: DeclarationSinistreRequest, fichiers: File[]): Observable<Sinistre> {
    const formData = new FormData();
    formData.append('declaration', new Blob([JSON.stringify(declaration)], { type: 'application/json' }));
    fichiers.forEach(fichier => formData.append('fichiers', fichier));
    return this.http.post<Sinistre>(this.API_URL, formData);
  }

  listerSinistres(): Observable<Sinistre[]> {
    return this.http.get<Sinistre[]>(this.API_URL);
  }

  getSinistre(id: number): Observable<Sinistre> {
    return this.http.get<Sinistre>(`${this.API_URL}/${id}`);
  }

  getPieceJointeUrl(sinistreId: number, pieceId: number): string {
    return `${this.API_URL}/${sinistreId}/pieces-jointes/${pieceId}`;
  }
}
