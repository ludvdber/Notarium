import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const content: Sx = { textAlign: 'center' };

export const avatar: Sx = {
  width: 56,
  height: 56,
  mx: 'auto',
  mb: 1,
  bgcolor: 'primary.main',
};

export const name: Sx = { fontWeight: 700 };

export const socialRow: Sx = {
  display: 'flex',
  gap: 1,
  justifyContent: 'center',
  mt: 1.5,
};

export const socialIcon: Sx = {
  fontSize: 18,
  color: 'text.secondary',
};

export const badgesRow: Sx = {
  display: 'flex',
  gap: 0.5,
  flexWrap: 'wrap',
  justifyContent: 'center',
  mt: 2,
};
