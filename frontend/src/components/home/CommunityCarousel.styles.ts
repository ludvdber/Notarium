import type { SxProps, Theme } from '@mui/material';
import type { CSSProperties } from 'react';

type Sx = SxProps<Theme>;

export const section: Sx = { py: { xs: 6, md: 9 }, position: 'relative' };

export const title: Sx = {
  textAlign: 'center',
  mb: 3,
  fontWeight: 800,
};

export const emptyCard: Sx = {
  p: 5,
  textAlign: 'center',
  maxWidth: 440,
  mx: 'auto',
};

export const emptyIcon: Sx = { fontSize: 40, mb: 1 };

export const emptyText: Sx = { fontWeight: 600 };

export const marqueeViewport: Sx = { overflow: 'hidden', position: 'relative' };

export const fadeEdgeLeft: Sx = {
  position: 'absolute',
  left: 0,
  top: 0,
  bottom: 0,
  width: 60,
  zIndex: 2,
  background: 'linear-gradient(90deg, var(--bg, rgba(10,14,26,1)), transparent)',
  pointerEvents: 'none',
};

export const fadeEdgeRight: Sx = {
  position: 'absolute',
  right: 0,
  top: 0,
  bottom: 0,
  width: 60,
  zIndex: 2,
  background: 'linear-gradient(270deg, var(--bg, rgba(10,14,26,1)), transparent)',
  pointerEvents: 'none',
};

export const marqueeTrack: Sx = {
  display: 'flex',
  gap: 2,
  animation: 'marquee 40s linear infinite',
  width: 'max-content',
  '&:hover': { animationPlayState: 'paused' },
  '@keyframes marquee': {
    '0%': { transform: 'translateX(0)' },
    '100%': { transform: 'translateX(-50%)' },
  },
};

export const profileWrapper: Sx = {
  minWidth: 200,
  cursor: 'pointer',
  flexShrink: 0,
};

export const profileCard: Sx = {
  p: 2,
  textAlign: 'center',
  '&:hover': { borderColor: 'primary.main' },
};

export const profileAvatar: Sx = {
  width: 48,
  height: 48,
  mx: 'auto',
  mb: 1,
  bgcolor: 'primary.main',
  fontSize: 18,
};

export const profileName: Sx = { fontWeight: 700 };

export const profileMeta: Sx = {
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  gap: 0.5,
  mt: 0.5,
};

export const discordPill: Sx = {
  bgcolor: '#5865F2',
  color: '#fff',
  fontSize: 10,
  px: 0.8,
  py: 0.2,
  borderRadius: 1,
  fontWeight: 600,
};

export const profileBadges: Sx = {
  display: 'flex',
  gap: 0.5,
  justifyContent: 'center',
  mt: 1,
  flexWrap: 'wrap',
};

export const popupWrapper: CSSProperties = {
  position: 'fixed',
  bottom: 24,
  left: '50%',
  transform: 'translateX(-50%)',
  zIndex: 1300,
  width: 320,
};

export const popupCard: Sx = {
  p: 3,
  position: 'relative',
  boxShadow: '0 8px 40px rgba(0,0,0,0.5)',
};

export const popupClose: Sx = {
  position: 'absolute',
  top: 8,
  right: 8,
};

export const popupBody: Sx = { textAlign: 'center' };

export const popupAvatar: Sx = {
  width: 56,
  height: 56,
  mx: 'auto',
  mb: 1,
  bgcolor: 'primary.main',
};

export const popupName: Sx = { fontWeight: 700 };

export const popupActions: Sx = {
  display: 'flex',
  gap: 1.5,
  justifyContent: 'center',
  mt: 2,
};

export const popupDiscordPill: Sx = {
  bgcolor: '#5865F2',
  color: '#fff',
  px: 2,
  py: 0.5,
  borderRadius: 2,
  fontSize: 13,
  fontWeight: 600,
};

export const popupBadges: Sx = {
  display: 'flex',
  gap: 0.5,
  justifyContent: 'center',
  mt: 2,
  flexWrap: 'wrap',
};
