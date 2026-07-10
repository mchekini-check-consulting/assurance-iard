import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DevisService } from '../../core/services/devis.service';
import { AuthService } from '../../core/services/auth.service';
import {
  Devis, DevisRequest, TypeBien, TypeResidence, StatutOccupation,
  Formule, StatutDevis
} from '../../core/models/devis.model';

@Component({
  selector: 'app-devis-wizard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './devis-wizard.component.html',
  styleUrl: './devis-wizard.component.scss'
})
export class DevisWizardComponent implements OnInit {
  currentStep = signal(1);
  totalSteps = 6;
  devis = signal<Devis | null>(null);
  isLoading = signal(false);
  isSaving = signal(false);
  errorMessage = signal('');

  // Formulaires par étape
  formBien!: FormGroup;
  formOccupation!: FormGroup;
  formSecurite!: FormGroup;
  formContenu!: FormGroup;
  formAntecedents!: FormGroup;
  formFormule!: FormGroup;

  // Options
  typesBien = Object.values(TypeBien);
  typesResidence = Object.values(TypeResidence);
  statutsOccupation = Object.values(StatutOccupation);
  formules = Object.values(Formule);

  optionsGaranties = [
    { code: 'BRIS_GLACE', label: 'Bris de glace', prix: 24 },
    { code: 'VOL_HORS_DOMICILE', label: 'Vol hors domicile', prix: 36 },
    { code: 'JARDIN', label: 'Protection jardin', prix: 18 },
    { code: 'PISCINE_PLUS', label: 'Piscine Plus', prix: 45 },
    { code: 'DOMMAGES_ELECTRIQUES', label: 'Dommages électriques', prix: 30 },
    { code: 'ASSISTANCE_PLUS', label: 'Assistance Plus 24/7', prix: 42 }
  ];

  steps = [
    { number: 1, title: 'Le bien', icon: '🏠' },
    { number: 2, title: 'Occupation', icon: '🔑' },
    { number: 3, title: 'Sécurité', icon: '🔒' },
    { number: 4, title: 'Contenu', icon: '📦' },
    { number: 5, title: 'Antécédents', icon: '📋' },
    { number: 6, title: 'Formule', icon: '✨' }
  ];

  progress = computed(() => ((this.currentStep() - 1) / (this.totalSteps - 1)) * 100);

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private devisService: DevisService,
    public authService: AuthService
  ) {
    this.initForms();
  }

  ngOnInit(): void {
    const devisId = this.route.snapshot.params['id'];
    if (devisId) {
      this.loadDevis(+devisId);
    } else {
      this.createNewDevis();
    }
  }

  private initForms(): void {
    this.formBien = this.fb.group({
      typeBien: ['', Validators.required],
      typeResidence: ['', Validators.required],
      adresse: ['', [Validators.required, Validators.minLength(5)]],
      codePostal: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],
      ville: ['', Validators.required],
      surfaceHabitable: ['', [Validators.required, Validators.min(9), Validators.max(500)]],
      nombrePieces: ['', [Validators.required, Validators.min(1), Validators.max(20)]],
      etage: [''],
      anneeConstruction: ['', [Validators.required, Validators.min(1800), Validators.max(new Date().getFullYear())]]
    });

    this.formOccupation = this.fb.group({
      statutOccupation: ['', Validators.required]
    });

    this.formSecurite = this.fb.group({
      alarme: [false],
      porteBlindee: [false],
      dependances: [false],
      surfaceDependances: [''],
      piscine: [false]
    });

    this.formContenu = this.fb.group({
      capitalMobilier: ['', [Validators.required, Validators.min(5000), Validators.max(500000)]],
      objetsValeur: [false],
      valeurObjetsValeur: ['']
    });

    this.formAntecedents = this.fb.group({
      nombreSinistres36Mois: [0, [Validators.required, Validators.min(0), Validators.max(10)]]
    });

    this.formFormule = this.fb.group({
      formule: ['', Validators.required],
      optionsGaranties: [[]]
    });

    // Watcher pour afficher/masquer les champs conditionnels
    this.formBien.get('typeBien')?.valueChanges.subscribe(type => {
      const etageControl = this.formBien.get('etage');
      if (type === TypeBien.APPARTEMENT) {
        etageControl?.setValidators([Validators.required, Validators.min(0), Validators.max(50)]);
      } else {
        etageControl?.clearValidators();
        etageControl?.setValue('');
      }
      etageControl?.updateValueAndValidity();
    });

    this.formSecurite.get('dependances')?.valueChanges.subscribe(hasDep => {
      const surfaceControl = this.formSecurite.get('surfaceDependances');
      if (hasDep) {
        surfaceControl?.setValidators([Validators.required, Validators.min(1)]);
      } else {
        surfaceControl?.clearValidators();
        surfaceControl?.setValue('');
      }
      surfaceControl?.updateValueAndValidity();
    });

    this.formContenu.get('objetsValeur')?.valueChanges.subscribe(hasObjets => {
      const valeurControl = this.formContenu.get('valeurObjetsValeur');
      if (hasObjets) {
        valeurControl?.setValidators([Validators.required, Validators.min(1000)]);
      } else {
        valeurControl?.clearValidators();
        valeurControl?.setValue('');
      }
      valeurControl?.updateValueAndValidity();
    });
  }

  private createNewDevis(): void {
    this.isLoading.set(true);
    this.devisService.creerDevis().subscribe({
      next: (devis) => {
        this.devis.set(devis);
        this.isLoading.set(false);
        // Mettre à jour l'URL sans recharger
        this.router.navigate(['/devis', devis.id], { replaceUrl: true });
      },
      error: (error) => {
        this.errorMessage.set('Erreur lors de la création du devis');
        this.isLoading.set(false);
      }
    });
  }

  private loadDevis(id: number): void {
    this.isLoading.set(true);
    this.devisService.getDevis(id).subscribe({
      next: (devis) => {
        this.devis.set(devis);
        this.currentStep.set(devis.etapeCourante || 1);
        this.populateForms(devis);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set('Devis non trouvé');
        this.isLoading.set(false);
        this.router.navigate(['/dashboard/devis']);
      }
    });
  }

  private populateForms(devis: Devis): void {
    if (!devis.donneesRisque) return;
    const d = devis.donneesRisque;

    this.formBien.patchValue({
      typeBien: d.typeBien || '',
      typeResidence: d.typeResidence || '',
      adresse: d.adresse || '',
      codePostal: d.codePostal || '',
      ville: d.ville || '',
      surfaceHabitable: d.surfaceHabitable || '',
      nombrePieces: d.nombrePieces || '',
      etage: d.etage || '',
      anneeConstruction: d.anneeConstruction || ''
    });

    this.formOccupation.patchValue({
      statutOccupation: d.statutOccupation || ''
    });

    this.formSecurite.patchValue({
      alarme: d.alarme || false,
      porteBlindee: d.porteBlindee || false,
      dependances: d.dependances || false,
      surfaceDependances: d.surfaceDependances || '',
      piscine: d.piscine || false
    });

    this.formContenu.patchValue({
      capitalMobilier: d.capitalMobilier || '',
      objetsValeur: d.objetsValeur || false,
      valeurObjetsValeur: d.valeurObjetsValeur || ''
    });

    this.formAntecedents.patchValue({
      nombreSinistres36Mois: d.nombreSinistres36Mois ?? 0
    });

    this.formFormule.patchValue({
      formule: d.formule || '',
      optionsGaranties: d.optionsGaranties || []
    });
  }

  getCurrentForm(): FormGroup {
    switch (this.currentStep()) {
      case 1: return this.formBien;
      case 2: return this.formOccupation;
      case 3: return this.formSecurite;
      case 4: return this.formContenu;
      case 5: return this.formAntecedents;
      case 6: return this.formFormule;
      default: return this.formBien;
    }
  }

  isCurrentStepValid(): boolean {
    return this.getCurrentForm().valid;
  }

  nextStep(): void {
    if (!this.isCurrentStepValid()) {
      this.getCurrentForm().markAllAsTouched();
      return;
    }
    this.saveCurrentStep();
    if (this.currentStep() < this.totalSteps) {
      this.currentStep.update(s => s + 1);
    }
  }

  previousStep(): void {
    if (this.currentStep() > 1) {
      this.currentStep.update(s => s - 1);
    }
  }

  goToStep(step: number): void {
    if (step <= this.currentStep() || this.isCurrentStepValid()) {
      this.saveCurrentStep();
      this.currentStep.set(step);
    }
  }

  private saveCurrentStep(): void {
    const devisId = this.devis()?.id;
    if (!devisId) return;

    this.isSaving.set(true);
    const request = this.buildRequest();

    this.devisService.sauvegarderEtape(devisId, request).subscribe({
      next: (devis) => {
        this.devis.set(devis);
        this.isSaving.set(false);
      },
      error: () => {
        this.isSaving.set(false);
      }
    });
  }

  private buildRequest(): DevisRequest {
    return {
      etapeCourante: this.currentStep(),
      ...this.formBien.value,
      ...this.formOccupation.value,
      ...this.formSecurite.value,
      ...this.formContenu.value,
      ...this.formAntecedents.value,
      ...this.formFormule.value,
      souscripteurEstAssure: true
    };
  }

  calculerTarif(): void {
    if (!this.isCurrentStepValid()) {
      this.getCurrentForm().markAllAsTouched();
      return;
    }

    const devisId = this.devis()?.id;
    if (!devisId) return;

    this.isLoading.set(true);

    // D'abord sauvegarder, puis tarifier
    const request = this.buildRequest();
    this.devisService.sauvegarderEtape(devisId, request).subscribe({
      next: () => {
        this.devisService.calculerTarif(devisId).subscribe({
          next: (devis) => {
            this.devis.set(devis);
            this.isLoading.set(false);
            this.router.navigate(['/devis', devisId, 'resultat']);
          },
          error: (error) => {
            this.errorMessage.set('Erreur lors du calcul du tarif');
            this.isLoading.set(false);
          }
        });
      },
      error: () => {
        this.errorMessage.set('Erreur lors de la sauvegarde');
        this.isLoading.set(false);
      }
    });
  }

  sauvegarderEtQuitter(): void {
    this.saveCurrentStep();
    this.router.navigate(['/dashboard/devis']);
  }

  toggleOption(code: string): void {
    const current = this.formFormule.get('optionsGaranties')?.value || [];
    const index = current.indexOf(code);
    if (index > -1) {
      current.splice(index, 1);
    } else {
      current.push(code);
    }
    this.formFormule.get('optionsGaranties')?.setValue([...current]);
  }

  isOptionSelected(code: string): boolean {
    const current = this.formFormule.get('optionsGaranties')?.value || [];
    return current.includes(code);
  }

  getTypeBienLabel(type: TypeBien): string {
    return type === TypeBien.APPARTEMENT ? 'Appartement' : 'Maison';
  }

  getTypeResidenceLabel(type: TypeResidence): string {
    return type === TypeResidence.PRINCIPALE ? 'Résidence principale' : 'Résidence secondaire';
  }

  getStatutOccupationLabel(statut: StatutOccupation): string {
    switch (statut) {
      case StatutOccupation.PROPRIETAIRE_OCCUPANT: return 'Propriétaire occupant';
      case StatutOccupation.LOCATAIRE: return 'Locataire';
      case StatutOccupation.PROPRIETAIRE_NON_OCCUPANT: return 'Propriétaire non occupant';
    }
  }

  getFormuleLabel(formule: Formule): string {
    switch (formule) {
      case Formule.ESSENTIELLE: return 'Essentielle';
      case Formule.CONFORT: return 'Confort';
      case Formule.PREMIUM: return 'Premium';
    }
  }
}
