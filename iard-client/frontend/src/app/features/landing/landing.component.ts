import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import {
  LucideShieldCheck,
  LucideMenu,
  LucideSparkles,
  LucideCarFront,
  LucideHouse,
  LucideBriefcaseBusiness,
  LucideStar,
  LucideZap,
  LucideSearch,
  LucideUnlock,
  LucideMessagesSquare,
  LucidePlus
} from '@lucide/angular';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    LucideShieldCheck,
    LucideMenu,
    LucideSparkles,
    LucideCarFront,
    LucideHouse,
    LucideBriefcaseBusiness,
    LucideStar,
    LucideZap,
    LucideSearch,
    LucideUnlock,
    LucideMessagesSquare,
    LucidePlus
  ],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss'
})
export class LandingComponent {
  mobileMenuOpen = false;

  products = [
    {
      id: 'auto',
      title: 'Assurance Auto',
      description: 'Protégez votre véhicule avec une couverture adaptée à vos besoins.',
      available: false
    },
    {
      id: 'habitation',
      title: 'Assurance Habitation',
      description: 'Sécurisez votre logement et vos biens contre tous les risques.',
      available: true
    },
    {
      id: 'rcpro',
      title: 'RC Professionnelle',
      description: 'Couvrez votre activité professionnelle en toute sérénité.',
      available: false
    }
  ];

  steps = [
    {
      number: 1,
      title: 'Décrivez votre besoin',
      description: 'Quelques questions simples pour cerner ce que vous voulez protéger.'
    },
    {
      number: 2,
      title: 'Recevez votre devis',
      description: 'Un tarif clair et personnalisé, affiché instantanément.'
    },
    {
      number: 3,
      title: 'Activez votre contrat',
      description: 'Signature en ligne et attestation immédiate dans votre espace.'
    }
  ];

  testimonials = [
    {
      name: 'Marie Dupont',
      rating: 5,
      comment: 'Service rapide et efficace ! J\'ai obtenu mon devis en quelques minutes.',
      role: 'Cliente Habitation'
    },
    {
      name: 'Pierre Martin',
      rating: 5,
      comment: 'Excellent rapport qualité-prix. Je recommande vivement.',
      role: 'Client Habitation'
    },
    {
      name: 'Sophie Bernard',
      rating: 4,
      comment: 'Très satisfaite de la prise en charge de mon sinistre.',
      role: 'Cliente Habitation'
    },
    {
      name: 'Jean Lefebvre',
      rating: 5,
      comment: 'Une équipe à l\'écoute et des tarifs vraiment compétitifs.',
      role: 'Client Habitation'
    },
    {
      name: 'Amélie Roux',
      rating: 5,
      comment: 'Souscription 100% en ligne, c\'était fluide du début à la fin.',
      role: 'Cliente Habitation'
    },
    {
      name: 'Karim Benali',
      rating: 5,
      comment: 'Remboursement traité en 24h, je n\'en reviens toujours pas.',
      role: 'Client Habitation'
    }
  ];

  advantages = [
    {
      icon: 'zap',
      title: 'Rapidité',
      description: 'Obtenez votre devis en moins de 2 minutes.'
    },
    {
      icon: 'search',
      title: 'Transparence',
      description: 'Des tarifs clairs, sans frais cachés.'
    },
    {
      icon: 'unlock',
      title: 'Sans engagement',
      description: 'Résiliez à tout moment, en un clic.'
    },
    {
      icon: 'messages',
      title: 'Support réactif',
      description: 'Une équipe disponible 7j/7 pour vous aider.'
    }
  ];

  stats = [
    { value: '50 000+', label: 'Clients satisfaits' },
    { value: '98%', label: 'Satisfaction' },
    { value: '24h', label: 'Remboursement' },
    { value: '15 ans', label: 'D\'expérience' }
  ];

  faqs = [
    {
      question: 'Comment souscrire à une assurance ?',
      answer: 'Remplissez le formulaire en ligne, recevez votre devis personnalisé puis signez électroniquement. Votre attestation est disponible immédiatement.'
    },
    {
      question: 'Quels sont les délais de remboursement ?',
      answer: 'La plupart des dossiers complets sont remboursés sous 24 à 48h après validation.'
    },
    {
      question: 'Puis-je modifier mon contrat ?',
      answer: 'Oui, à tout moment depuis votre espace client, sans frais ni paperasse.'
    }
  ];

  toggleMobileMenu(): void {
    this.mobileMenuOpen = !this.mobileMenuOpen;
  }

  getStars(rating: number): number[] {
    return Array(rating).fill(0);
  }

  getEmptyStars(rating: number): number[] {
    return Array(5 - rating).fill(0);
  }

  getInitials(name: string): string {
    return name.split(' ').map(n => n.charAt(0)).join('').toUpperCase();
  }
}
