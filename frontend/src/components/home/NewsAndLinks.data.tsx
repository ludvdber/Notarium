import type { ReactNode } from 'react';
import {
  School,
  Description,
  Email,
  Schedule,
  Assignment,
  Work,
  BusinessCenter,
  MenuBook,
  Lightbulb,
} from '@mui/icons-material';

export interface MainLink {
  key: string;
  icon: ReactNode;
  color: string;
  url: string;
}

export interface SecondaryLink {
  key: string;
  icon: ReactNode;
  url: string;
}

// URLs from https://www.isfce.org (verified 2026-04-11)
export const MAIN_LINKS: MainLink[] = [
  { key: 'moodle', icon: <School />, color: '#f59e0b', url: 'https://moodle.isfce.be' },
  { key: 'bulletin', icon: <Description />, color: '#3b82f6', url: 'https://www.isfce.org/Utilitaire/Resu/' },
  { key: 'mail', icon: <Email />, color: '#00d2ff', url: 'https://webmail.isfce.be/' },
];

export const SECONDARY_LINKS: SecondaryLink[] = [
  { key: 'horaires', icon: <Schedule />, url: 'https://www.isfce.org/index.php?pg=horaires' },
  { key: 'tfe', icon: <Assignment />, url: 'https://www.isfce.org/Utilitaire/SujetMemoire/' },
  { key: 'stages', icon: <Work />, url: 'https://www.isfce.org/index.php?pg=offres_stages' },
  { key: 'emplois', icon: <BusinessCenter />, url: 'https://www.isfce.org/index.php?pg=offres_emplois' },
  { key: 'anciensTfe', icon: <MenuBook />, url: 'https://www.isfce.org/Utilitaire/Bibliothèque/' },
  { key: 'aideReussite', icon: <Lightbulb />, url: 'https://form.jotform.com/lisfce/retourlisteateliers' },
];
