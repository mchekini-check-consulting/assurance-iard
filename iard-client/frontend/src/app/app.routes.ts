import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/landing/landing.component').then(m => m.LandingComponent)
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent),
    canActivate: [guestGuard]
  },
  {
    path: 'coming-soon',
    loadComponent: () => import('./features/coming-soon/coming-soon.component').then(m => m.ComingSoonComponent)
  },
  {
    path: 'devis',
    loadComponent: () => import('./features/devis-wizard/devis-wizard.component').then(m => m.DevisWizardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'devis/:id',
    loadComponent: () => import('./features/devis-wizard/devis-wizard.component').then(m => m.DevisWizardComponent),
    canActivate: [authGuard]
  },
  {
    path: 'devis/:id/resultat',
    loadComponent: () => import('./features/devis-wizard/devis-resultat.component').then(m => m.DevisResultatComponent),
    canActivate: [authGuard]
  },
  {
    path: 'contrat/:id',
    loadComponent: () => import('./features/contrat/contrat-view.component').then(m => m.ContratViewComponent),
    canActivate: [authGuard]
  },
  {
    path: 'contrat/:id/modifier',
    loadComponent: () => import('./features/contrat/modifier-garanties.component').then(m => m.ModifierGarantiesComponent),
    canActivate: [authGuard]
  },
  {
    path: 'kyc',
    loadComponent: () => import('./features/kyc/kyc-verification.component').then(m => m.KycVerificationComponent),
    canActivate: [authGuard]
  },
  {
    path: 'dashboard',
    loadComponent: () => import('./features/dashboard/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
    canActivate: [authGuard],
    children: [
      {
        path: '',
        loadComponent: () => import('./features/dashboard/dashboard-home/dashboard-home.component').then(m => m.DashboardHomeComponent)
      },
      {
        path: 'devis',
        loadComponent: () => import('./features/dashboard/mes-devis/mes-devis.component').then(m => m.MesDevisComponent)
      },
      {
        path: 'contrats',
        loadComponent: () => import('./features/dashboard/mes-contrats/mes-contrats.component').then(m => m.MesContratsComponent)
      },
      {
        path: 'documents',
        loadComponent: () => import('./features/dashboard/mes-documents/mes-documents.component').then(m => m.MesDocumentsComponent)
      },
      {
        path: 'factures',
        loadComponent: () => import('./features/dashboard/mes-factures/mes-factures.component').then(m => m.MesFacturesComponent)
      },
      {
        path: 'sinistres',
        loadComponent: () => import('./features/dashboard/mes-sinistres/mes-sinistres.component').then(m => m.MesSinistresComponent)
      },
      {
        path: 'sinistres/declarer',
        loadComponent: () => import('./features/dashboard/declarer-sinistre/declarer-sinistre.component').then(m => m.DeclarerSinistreComponent)
      },
      {
        path: 'sinistres/:id',
        loadComponent: () => import('./features/dashboard/sinistre-detail/sinistre-detail.component').then(m => m.SinistreDetailComponent)
      },
      {
        path: 'profil',
        loadComponent: () => import('./features/dashboard/placeholder-page/placeholder-page.component').then(m => m.PlaceholderPageComponent),
        data: { icon: '👤', title: 'Mon profil', description: 'Gérez vos informations personnelles' }
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
];
