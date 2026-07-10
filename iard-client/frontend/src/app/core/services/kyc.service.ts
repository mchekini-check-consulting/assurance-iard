import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  KycStatusResponse,
  TitreSejourExtraction,
  RibExtraction,
  KycVerificationResult,
  DocumentResponse
} from '../models/kyc.model';

@Injectable({
  providedIn: 'root'
})
export class KycService {
  private readonly API_URL = '/api/kyc';

  constructor(private http: HttpClient) {}

  getStatus(): Observable<KycStatusResponse> {
    return this.http.get<KycStatusResponse>(`${this.API_URL}/status`);
  }

  uploadTitreSejour(file: File): Observable<TitreSejourExtraction> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<TitreSejourExtraction>(`${this.API_URL}/upload/titre-sejour`, formData);
  }

  uploadRib(file: File): Observable<RibExtraction> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<RibExtraction>(`${this.API_URL}/upload/rib`, formData);
  }

  verify(): Observable<KycVerificationResult> {
    return this.http.post<KycVerificationResult>(`${this.API_URL}/verify`, {});
  }

  getDocuments(): Observable<DocumentResponse[]> {
    return this.http.get<DocumentResponse[]>(`${this.API_URL}/documents`);
  }

  getDocumentUrl(id: number): string {
    return `${this.API_URL}/documents/${id}`;
  }
}
