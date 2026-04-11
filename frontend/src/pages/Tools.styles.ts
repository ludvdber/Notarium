import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const container: Sx = {
  py: 4,
  minHeight: 'calc(100vh - 200px)',
};

export const title: Sx = {
  fontWeight: 800,
  mb: 3,
  background: 'linear-gradient(135deg, #00d2ff, #7b2ff7)',
  backgroundClip: 'text',
  WebkitBackgroundClip: 'text',
  WebkitTextFillColor: 'transparent',
};

export const tabsCard: Sx = { mb: 3 };

export const tabs: Sx = {
  '& .MuiTab-root': {
    fontWeight: 600,
    py: 2,
    textTransform: 'none',
    fontSize: '0.95rem',
  },
};

export const tabIcon: Sx = { fontSize: 20 };
