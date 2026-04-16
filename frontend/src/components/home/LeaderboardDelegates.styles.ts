import type { SxProps, Theme } from '@mui/material';
import type { CSSProperties } from 'react';
import { TOKENS } from '@/theme/tokens';

type Sx = SxProps<Theme>;

export const outerGrid: Sx = { py: { xs: 6, md: 9 } };

export const columnTitle: Sx = { mb: 2, fontWeight: 800 };

export const columnHeader: Sx = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  mb: 2,
};

export const viewAllLink: Sx = {
  color: 'primary.main',
  textDecoration: 'none',
  fontSize: 13,
  fontWeight: 700,
  '&:hover': { textDecoration: 'underline' },
};

export const listCard: Sx = { p: 0 };

export const scrollableList: Sx = {
  maxHeight: 360,
  overflowY: 'auto',
  '&::-webkit-scrollbar': { width: 6 },
  '&::-webkit-scrollbar-track': { background: 'transparent' },
  '&::-webkit-scrollbar-thumb': {
    background: (t) => t.palette.mode === 'dark' ? 'rgba(255,255,255,0.15)' : 'rgba(0,0,0,0.15)',
    borderRadius: 3,
  },
  scrollbarWidth: 'thin',
};

export const delegatesCard: Sx = { p: 2.5, position: 'relative' };

export const entryRow: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 2,
  px: 2.5,
  py: 2,
  borderBottom: (t) => `1px solid ${t.palette.mode === 'dark' ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.06)'}`,
  '&:last-child': { borderBottom: 'none' },
  textDecoration: 'none',
  color: 'inherit',
  transition: 'background-color 0.15s',
  '&:hover': { bgcolor: 'action.hover' },
};

export const medalCircle = (rank: number, color: string): Sx => ({
  width: 36,
  height: 36,
  borderRadius: '50%',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  bgcolor: rank <= 3 ? `${color}20` : 'transparent',
  flexShrink: 0,
});

export const medalIcon = (color: string): Sx => ({ fontSize: 20, color });

export const medalRank: Sx = { fontWeight: 700, color: 'text.secondary' };

export const userInfo: Sx = { flex: 1, minWidth: 0 };

export const userNameRow: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 1,
};

export const userName: Sx = { fontWeight: 700 };

export const supporterChip: Sx = { fontSize: 10, height: 20 };

export const supporterIcon: Sx = { fontSize: '12px !important' };

export const badgesRow: Sx = {
  display: 'flex',
  gap: 0.5,
  mt: 0.5,
  flexWrap: 'wrap',
};

export const xpCol: Sx = { textAlign: 'right', flexShrink: 0 };

export const xpValue: Sx = {
  fontWeight: 800,
  color: 'primary.main',
  fontSize: '1.1rem',
};

export const emptyState: Sx = { p: 4, textAlign: 'center' };

export const emptyIcon: Sx = { fontSize: 32, mb: 1 };

export const delegateBlock: Sx = {
  mb: 2,
  '&:last-child': { mb: 0 },
};

export const delegateSectionName: Sx = { mb: 1, fontWeight: 600 };

export const delegateMembers: Sx = {
  display: 'flex',
  gap: 1,
  flexWrap: 'wrap',
};

export const delegateChip: Sx = {
  cursor: 'pointer',
  '&:focus-visible': {
    outline: (t) => `2px solid ${t.palette.primary.main}`,
    outlineOffset: 2,
  },
};

export const discordPopup: CSSProperties = {
  position: 'absolute',
  bottom: 12,
  right: 12,
  zIndex: 10,
};

export const discordBox: Sx = {
  bgcolor: TOKENS.brands.discord,
  color: '#fff',
  px: 2,
  py: 1,
  borderRadius: 2,
  display: 'flex',
  alignItems: 'center',
  gap: 1,
  boxShadow: `0 4px 20px ${TOKENS.brands.discordShadow}`,
};

export const discordName: Sx = { fontWeight: 600 };

export const discordHandle: Sx = { opacity: 0.8 };
