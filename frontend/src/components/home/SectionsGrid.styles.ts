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

export const grid: Sx = {
  display: 'grid',
  gridTemplateColumns: {
    xs: 'repeat(2, 1fr)',
    sm: 'repeat(3, 1fr)',
    md: 'repeat(auto-fit, minmax(170px, 1fr))',
  },
  gap: 2,
};

export const card = (color: string): Sx => ({
  p: 2.5,
  textAlign: 'center',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  textDecoration: 'none',
  height: '100%',
  cursor: 'pointer',
  position: 'relative',
  overflow: 'hidden',
  transition: 'border-color 0.25s, transform 0.25s',
  '&::before': {
    content: '""',
    position: 'absolute',
    inset: 0,
    background: `radial-gradient(circle at 50% 0%, ${color}14 0%, transparent 60%)`,
    opacity: 0,
    transition: 'opacity 0.3s',
    pointerEvents: 'none',
  },
  '&:hover': {
    borderColor: `${color}55`,
  },
  '&:hover::before': { opacity: 1 },
});

export const icon: Sx = { fontSize: 26, mb: 0.75, lineHeight: 1 };

export const name: Sx = { fontWeight: 800, mb: 0.5, lineHeight: 1.2 };

export const count = (color: string): Sx => ({
  color,
  fontWeight: 800,
});
