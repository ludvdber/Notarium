import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const title: Sx = { fontWeight: 700, mb: 1 };

export const subtitle: Sx = { mb: 1 };

export const badgesRow: Sx = {
  display: 'flex',
  gap: 0.5,
  flexWrap: 'wrap',
  mb: 3,
};

export const successAlert: Sx = { mb: 2 };

export const form: Sx = {
  display: 'flex',
  flexDirection: 'column',
  gap: 2,
};
