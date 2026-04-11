import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const section: Sx = { py: { xs: 6, md: 9 } };

export const header: Sx = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  mb: 3,
};

export const title: Sx = { fontWeight: 800 };

export const viewAllLink: Sx = {
  color: '#00d2ff',
  textDecoration: 'none',
  fontSize: 13,
  fontWeight: 700,
  '&:hover': { textDecoration: 'underline' },
};

export const card = (color: string): Sx => ({
  p: 3,
  textAlign: 'center',
  display: 'block',
  textDecoration: 'none',
  height: '100%',
  cursor: 'pointer',
  transition: 'border-color 0.25s',
  '&:hover': { borderColor: `${color}40` },
});

export const icon: Sx = { fontSize: 30, mb: 1 };

export const name: Sx = { fontWeight: 800, mb: 0.5, lineHeight: 1.2 };

export const count = (color: string): Sx => ({
  color,
  fontWeight: 800,
});
