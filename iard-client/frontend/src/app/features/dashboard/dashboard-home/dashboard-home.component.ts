import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DevisService } from '../../../core/services/devis.service';
import { ContratService } from '../../../core/services/contrat.service';
import { LucidePenLine, LucideCheckCheck, LucideShieldCheck, LucideFileSignature, LucideHome, LucideArrowRight, LucideCarFront, LucideBriefcaseBusiness, LucideClipboardList } from '@lucide/angular';

@Component({
  selector: 'app-dashboard-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    LucidePenLine,
    LucideCheckCheck,
    LucideShieldCheck,
    LucideFileSignature,
    LucideHome,
    LucideArrowRight,
    LucideCarFront,
    LucideBriefcaseBusiness,
    LucideClipboardList
  ],
  templateUrl: './dashboard-home.component.html',
  styleUrl: './dashboard-home.component.scss'
})
export class DashboardHomeComponent implements OnInit {
  devisEnCours = signal(0);
  devisTermines = signal(0);
  contratsActifs = signal(0);
  contratsEnAttente = signal(0);

  constructor(
    public authService: AuthService,
    private devisService: DevisService,
    private contratService: ContratService
  ) {}

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.devisService.getStats().subscribe({
      next: (stats) => {
        this.devisEnCours.set(stats.brouillons);
        this.devisTermines.set(stats.devis);
      }
    });

    this.contratService.getStats().subscribe({
      next: (stats) => {
        this.contratsActifs.set(stats.actifs);
        this.contratsEnAttente.set(stats.enAttente);
      }
    });
  }
}
