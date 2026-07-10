export interface Facture {
  id: number;
  numeroFacture: string;
  contratId: number;
  numeroContrat: string;
  produit: string;
  montantHT: number;
  taxes: number;
  montantTTC: number;
  periode: string;
  dateEmission: string;
  datePaiement: string;
  pdfUrl: string;
  createdAt: string;
}
