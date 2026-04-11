import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const title: Sx = { fontWeight: 700, mb: 3 };

export const errorAlert: Sx = { mb: 2 };

export const form: Sx = {
  display: 'flex',
  flexDirection: 'column',
  gap: 2,
};
