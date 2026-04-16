import type { SxProps, Theme } from '@mui/material';
import { TOKENS } from '@/theme/tokens';

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
  background: TOKENS.gradients.primaryBar,
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
  background: TOKENS.gradients.primary,
  boxShadow: TOKENS.glow.primaryShadowStrong,
  '&:hover': {
    background: TOKENS.gradients.primaryHover,
  },
};

export const discordLink: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 0.75,
  color: TOKENS.brands.discord,
  fontWeight: 700,
  fontSize: 12,
  textDecoration: 'none',
  '&:hover': { textDecoration: 'underline' },
};

export const stickyBar: Sx = {
  display: { xs: 'flex', md: 'none' },
  position: 'fixed',
  bottom: 0,
  left: 0,
  right: 0,
  zIndex: 1200,
  p: 1.5,
  gap: 1,
  alignItems: 'center',
  justifyContent: 'space-between',
  bgcolor: 'rgba(10, 14, 26, 0.92)',
  backdropFilter: 'blur(16px)',
  WebkitBackdropFilter: 'blur(16px)',
  borderTop: '1px solid rgba(0,210,255,0.25)',
  boxShadow: '0 -8px 24px rgba(0,0,0,0.4)',
};

export const stickyLabel: Sx = {
  fontSize: 13,
  fontWeight: 700,
  flex: 1,
  minWidth: 0,
};

export const stickyCta: Sx = {
  px: 2.5,
  py: 0.75,
  fontSize: 12,
  fontWeight: 800,
  background: TOKENS.gradients.primary,
  flexShrink: 0,
  '&:hover': { background: TOKENS.gradients.primaryHover },
};
