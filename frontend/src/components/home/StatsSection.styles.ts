import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const section: Sx = { py: { xs: 6, md: 9 } };

export const card: Sx = { p: { xs: 2.5, md: 3 }, textAlign: 'center' };

export const icon: Sx = { fontSize: 22, mb: 1 };

export const value = (color: string): Sx => ({
  fontWeight: 900,
  color,
  fontSize: { xs: 22, md: 28 },
  lineHeight: 1.1,
  mb: 0.5,
});

export const label: Sx = { fontSize: 11, fontWeight: 600, letterSpacing: 0.2 };

export const weekBadgeWrapper: Sx = {
  display: 'flex',
  justifyContent: 'center',
  mt: 3,
};

export const weekBadge: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 0.75,
  px: 2,
  py: 0.6,
  borderRadius: 3,
  bgcolor: 'rgba(16, 185, 129, 0.08)',
  border: '1px solid rgba(16, 185, 129, 0.2)',
  textDecoration: 'none',
  transition: 'all 0.2s ease',
  '&:hover': {
    bgcolor: 'rgba(16, 185, 129, 0.14)',
    borderColor: 'rgba(16, 185, 129, 0.35)',
    transform: 'translateY(-1px)',
  },
};

export const weekBadgeIcon: Sx = { fontSize: 16, color: '#10b981' };

export const weekBadgeCount: Sx = { color: '#10b981', fontWeight: 800 };

export const weekBadgeLabel: Sx = { color: '#10b981', fontWeight: 500 };

export const shimmerItem: Sx = {
  borderRadius: 3,
  bgcolor: 'rgba(255,255,255,0.04)',
};
