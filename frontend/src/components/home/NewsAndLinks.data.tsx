import type { ReactNode } from 'react';
import { School, Description, Email, Schedule, Assignment, Work, BusinessCenter } from '@mui/icons-material';

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

export const MAIN_LINKS: MainLink[] = [
  { key: 'moodle', icon: <School />, color: '#f59e0b', url: 'https://moodle.isfce.be' },
  { key: 'bulletin', icon: <Description />, color: '#3b82f6', url: 'https://bulletin.isfce.be' },
  { key: 'mail', icon: <Email />, color: '#00d2ff', url: 'https://outlook.office.com' },
];

export const SECONDARY_LINKS: SecondaryLink[] = [
  { key: 'horaires', icon: <Schedule />, url: '#' },
  { key: 'tfe', icon: <Assignment />, url: '#' },
  { key: 'stages', icon: <Work />, url: '#' },
  { key: 'emplois', icon: <BusinessCenter />, url: '#' },
];
