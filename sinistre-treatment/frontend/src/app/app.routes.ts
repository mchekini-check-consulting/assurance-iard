import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/dossier-list/dossier-list.component').then(m => m.DossierListComponent)
  },
  {
    path: 'dossiers/:id',
    loadComponent: () => import('./features/dossier-detail/dossier-detail.component').then(m => m.DossierDetailComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
