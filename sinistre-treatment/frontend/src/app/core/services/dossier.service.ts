import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DecisionRequest, DossierSinistre } from '../models/dossier.model';

@Injectable({
  providedIn: 'root'
})
export class DossierService {
  private readonly API_URL = '/api/dossiers';

  constructor(private http: HttpClient) {}

  listerDossiers(): Observable<DossierSinistre[]> {
    return this.http.get<DossierSinistre[]>(this.API_URL);
  }

  getDossier(id: number): Observable<DossierSinistre> {
    return this.http.get<DossierSinistre>(`${this.API_URL}/${id}`);
  }

  decider(id: number, decision: DecisionRequest): Observable<DossierSinistre> {
    return this.http.post<DossierSinistre>(`${this.API_URL}/${id}/decision`, decision);
  }
}
