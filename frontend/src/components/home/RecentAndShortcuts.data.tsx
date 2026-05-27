import type { ReactNode } from 'react';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import FavoriteIcon from '@mui/icons-material/Favorite';
import SchoolIcon from '@mui/icons-material/School';
import LeaderboardIcon from '@mui/icons-material/Leaderboard';
import BugReportIcon from '@mui/icons-material/BugReport';
import VolunteerActivismIcon from '@mui/icons-material/VolunteerActivism';
import { KOFI_URL, GITHUB_URL } from '@/lib/constants';

export interface Shortcut {
  key: string;
  icon: ReactNode;
  to?: string;
  href?: string;
  requireVerified?: boolean;
}

export const SHORTCUTS: Shortcut[] = [
  { key: 'share',       icon: <CloudUploadIcon />,       to: '/upload',                requireVerified: true },
  { key: 'favorites',   icon: <FavoriteIcon />,          to: '/profile?tab=favorites' },
  { key: 'mySection',   icon: <SchoolIcon />,            to: '/browse' },
  { key: 'leaderboard', icon: <LeaderboardIcon />,       to: '/leaderboard' },
  { key: 'bug',         icon: <BugReportIcon />,         href: `${GITHUB_URL}/issues/new` },
  { key: 'kofi',        icon: <VolunteerActivismIcon />, href: KOFI_URL },
];
