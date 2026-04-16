import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const staggerVariants = {
  hidden: {},
  show: { transition: { staggerChildren: 0.15 } },
};

export const fadeUpVariants = {
  hidden: { opacity: 0, y: 30 },
  show: { opacity: 1, y: 0, transition: { duration: 0.6, ease: 'easeOut' as const } },
};

export const heroContainer: Sx = {
  position: 'relative',
  minHeight: '100vh',
  display: 'flex',
  alignItems: 'center',
  overflow: 'hidden',
};

export const scrollIndicator: Sx = {
  position: 'absolute',
  bottom: { xs: 60, md: 80 },
  left: '50%',
  transform: 'translateX(-50%)',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  gap: 0.5,
  color: 'text.secondary',
  cursor: 'pointer',
  zIndex: 2,
  userSelect: 'none',
  background: 'none',
  border: 'none',
  padding: 0,
  fontFamily: 'inherit',
  '&:hover': { color: 'primary.main' },
};

export const scrollIndicatorLabel: Sx = {
  fontSize: 11,
  fontWeight: 600,
  letterSpacing: 1,
  textTransform: 'uppercase',
  opacity: 0.8,
};

export const inner: Sx = {
  position: 'relative',
  zIndex: 1,
  textAlign: 'center',
};

export const title: Sx = {
  fontSize: { xs: '2.5rem', sm: '3.5rem', md: '4.5rem' },
  fontWeight: 900,
  lineHeight: 1.06,
  letterSpacing: -2,
  mb: 2.5,
};

export const titleGradient: Sx = {
  background: 'linear-gradient(135deg, #00d2ff, #7b2ff7)',
  backgroundClip: 'text',
  WebkitBackgroundClip: 'text',
  WebkitTextFillColor: 'transparent',
};

export const subtitle: Sx = {
  mb: 4,
  fontWeight: 400,
  fontSize: { xs: '1rem', md: '1.15rem' },
  maxWidth: 560,
  mx: 'auto',
  lineHeight: 1.7,
};

export const searchField = (theme: 'dark' | 'light'): Sx => ({
  maxWidth: 560,
  mx: 'auto',
  mb: 4,
  '& .MuiOutlinedInput-root': {
    borderRadius: 4,
    backdropFilter: 'blur(12px)',
    background: theme === 'dark' ? 'rgba(15, 20, 40, 0.6)' : 'rgba(255, 255, 255, 0.7)',
    fontSize: '1.1rem',
    py: 0.5,
  },
});

export const searchIcon: Sx = { color: 'text.secondary' };

export const ctaRow: Sx = {
  display: 'flex',
  gap: 2,
  justifyContent: 'center',
  flexWrap: 'wrap',
};

export const ctaPrimary: Sx = {
  px: 4,
  py: 1.5,
  fontSize: '1rem',
  background: 'linear-gradient(135deg, #00d2ff, #7b2ff7)',
  boxShadow: '0 4px 24px rgba(0,210,255,0.18)',
  '&:hover': {
    background: 'linear-gradient(135deg, #00b8d9, #6a1be0)',
  },
};

export const ctaSecondary: Sx = {
  px: 4,
  py: 1.5,
  fontSize: '1rem',
};
