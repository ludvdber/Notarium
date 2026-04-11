import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const wrapper: Sx = {
  my: { xs: 6, md: 9 },
};

export const card: Sx = {
  position: 'relative',
  overflow: 'hidden',
  p: { xs: 2.5, md: 3.5 },
};

export const gradientBar: Sx = {
  position: 'absolute',
  top: 0,
  left: 0,
  right: 0,
  height: 2,
  background: 'linear-gradient(90deg, #00d2ff, #7b2ff7)',
};

export const row: Sx = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  flexWrap: 'wrap',
  gap: 2,
};

export const textCol: Sx = {
  flex: 1,
  minWidth: 240,
};

export const title: Sx = {
  fontWeight: 900,
  fontSize: { xs: 18, md: 20 },
  lineHeight: 1.2,
  mb: 0.5,
};

export const subtitle: Sx = {
  fontSize: 13,
  color: 'text.secondary',
  lineHeight: 1.5,
  maxWidth: 440,
};

export const actions: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 2,
  flexShrink: 0,
};

export const primaryCta: Sx = {
  px: 3,
  py: 1,
  fontSize: 13,
  fontWeight: 800,
  background: 'linear-gradient(135deg, #00d2ff, #7b2ff7)',
  boxShadow: '0 4px 20px rgba(0,210,255,0.18)',
  '&:hover': {
    background: 'linear-gradient(135deg, #00b8d9, #6a1be0)',
  },
};

export const discordLink: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 0.75,
  color: '#5865F2',
  fontWeight: 700,
  fontSize: 12,
  textDecoration: 'none',
  '&:hover': { textDecoration: 'underline' },
};
