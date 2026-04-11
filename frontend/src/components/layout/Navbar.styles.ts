import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const appBar: Sx = {
  background: 'rgba(10, 14, 26, 0.8)',
  backdropFilter: 'blur(12px)',
  borderBottom: '1px solid rgba(255,255,255,0.06)',
  boxShadow: 'none',
};

export const toolbar: Sx = { justifyContent: 'space-between' };

export const logo: Sx = {
  fontWeight: 800,
  color: 'primary.main',
  textDecoration: 'none',
};

export const actionsRow: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 1,
};

export const avatar: Sx = { width: 32, height: 32, fontSize: 14 };
