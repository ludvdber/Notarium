import type { SxProps, Theme } from '@mui/material';
import type { CSSProperties } from 'react';
import { TOKENS } from '@/theme/tokens';

type Sx = SxProps<Theme>;

export const section: Sx = { py: { xs: 6, md: 9 } };

export const row: Sx = {
  display: 'flex',
  gap: 3,
  flexDirection: { xs: 'column', md: 'row' },
  alignItems: 'stretch',
};

export const delegatesCol: Sx = {
  flex: { xs: '1 1 auto', md: '2 1 0' },
  display: 'flex',
  flexDirection: 'column',
  minWidth: 0,
};

export const adCol: Sx = {
  flex: { xs: '1 1 auto', md: '1 1 0' },
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  minWidth: 0,
};

export const colTitle: Sx = { fontWeight: 800, mb: 2 };

export const delegatesCard: Sx = { p: 2.5, position: 'relative', flex: 1 };

export const emptyState: Sx = { p: 4, textAlign: 'center' };

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
