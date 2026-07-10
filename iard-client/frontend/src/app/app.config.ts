import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  provideLucideIcons,
  LucidePenLine,
  LucideCheckCheck,
  LucideShieldCheck,
  LucideFileSignature,
  LucideHome,
  LucideArrowRight,
  LucideCarFront,
  LucideBriefcaseBusiness,
  LucideClipboardList,
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
  LucideSparkles,
  LucideHouse,
  LucideStar,
  LucideZap,
  LucideUnlock,
  LucideMessagesSquare,
  LucidePlus
} from '@lucide/angular';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(withInterceptors([authInterceptor])),
    provideLucideIcons(
      LucidePenLine,
      LucideCheckCheck,
      LucideShieldCheck,
      LucideFileSignature,
      LucideHome,
      LucideArrowRight,
      LucideCarFront,
      LucideBriefcaseBusiness,
      LucideClipboardList,
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
      LucideSparkles,
      LucideHouse,
      LucideStar,
      LucideZap,
      LucideUnlock,
      LucideMessagesSquare,
      LucidePlus
    )
  ]
};
