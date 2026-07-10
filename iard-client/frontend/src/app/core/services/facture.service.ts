import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Facture } from '../models/facture.model';

@Injectable({
  providedIn: 'root'
})
export class FactureService {
  private readonly API_URL = '/api/factures';

  constructor(private http: HttpClient) {}

  getFactures(): Observable<Facture[]> {
    return this.http.get<Facture[]>(this.API_URL);
  }

  getFacture(id: number): Observable<Facture> {
    return this.http.get<Facture>(`${this.API_URL}/${id}`);
  }

  getPdfBlob(id: number): Observable<Blob> {
    return this.http.get(`${this.API_URL}/${id}/pdf`, {
      responseType: 'blob'
    });
  }
}
