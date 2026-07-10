import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import {
  LucideShieldCheck,
  LucidePanelLeftClose,
  LucideLayoutDashboard,
  LucideFileText,
  LucideFileCheck2,
  LucideFolder,
  LucideUserRound,
  LucideLogOut,
  LucideMenu,
  LucideSearch,
  LucideBell,
  LucideChevronDown,
  LucideReceipt,
  LucideTriangleAlert
} from '@lucide/angular';

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    LucideShieldCheck,
    LucidePanelLeftClose,
    LucideLayoutDashboard,
    LucideFileText,
    LucideFileCheck2,
    LucideFolder,
    LucideUserRound,
    LucideLogOut,
    LucideMenu,
    LucideSearch,
    LucideBell,
    LucideChevronDown,
    LucideReceipt,
    LucideTriangleAlert
  ],
  templateUrl: './dashboard-layout.component.html',
  styleUrl: './dashboard-layout.component.scss'
})
export class DashboardLayoutComponent {
  sidebarCollapsed = signal(false);

  menuItems = [
    { path: '/dashboard', label: 'Tableau de bord', icon: 'dashboard' },
    { path: '/dashboard/devis', label: 'Mes devis', icon: 'devis' },
    { path: '/dashboard/contrats', label: 'Mes contrats', icon: 'contrats' },
    { path: '/dashboard/factures', label: 'Facturation', icon: 'factures' },
    { path: '/dashboard/sinistres', label: 'Sinistres', icon: 'sinistres' },
    { path: '/dashboard/documents', label: 'Mes documents', icon: 'documents' },
    { path: '/dashboard/profil', label: 'Mon profil', icon: 'profil' }
  ];

  constructor(public authService: AuthService) {}

  toggleSidebar(): void {
    this.sidebarCollapsed.update(v => !v);
  }

  logout(): void {
    this.authService.logout();
  }

  getInitials(): string {
    const user = this.authService.currentUser();
    if (!user) return '';
    const prenom = user.prenom?.charAt(0) || '';
    const nom = user.nom?.charAt(0) || '';
    return (prenom + nom).toUpperCase();
  }
}
