// Central design tokens — all hardcoded colors used across the app live here.
// Theme augmentation in muiTheme.ts exposes these via `theme.palette.*`.

export const TOKENS = {
  gradients: {
    primary: 'linear-gradient(135deg, #00d2ff, #7b2ff7)',
    primaryHover: 'linear-gradient(135deg, #00b8d9, #6a1be0)',
    primaryBar: 'linear-gradient(90deg, #00d2ff, #7b2ff7)',
  },
  brands: {
    discord: '#5865F2',
    discordShadow: 'rgba(88, 101, 242, 0.4)',
  },
  medal: {
    gold: '#FFD700',
    silver: '#C0C0C0',
    bronze: '#CD7F32',
  },
  rating: {
    main: '#ffd93d',
  },
  fileTypes: {
    pdf: '#ef4444',
  },
  // Colors used by SectionsGrid cards and StatsSection icons.
  // Keyed by ISFCE bachelor/formation id.
  sections: {
    informatique: '#00d2ff',
    comptabilite: '#7b2ff7',
    marketing: '#ff6b9d',
    assistant: '#10b981',
    fiscalite: '#fbbf24',
    langues: '#f97316',
    cq6: '#a855f7',
  },
  stats: {
    docs: '#00d2ff',
    downloads: '#7b2ff7',
    contributors: '#ff6b9d',
    courses: '#10b981',
  },
  glow: {
    primaryShadow: '0 8px 32px rgba(0,210,255,0.08)',
    primaryShadowStrong: '0 4px 20px rgba(0,210,255,0.18)',
  },
  categories: {
    SYNTHESE: '#00d2ff',
    EXAMEN: '#ff6b6b',
    NOTES: '#ffd93d',
    EXERCICES: '#6bcb77',
    DIVERS: '#a66cff',
  },
} as const;

export type Tokens = typeof TOKENS;
