import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const drawerBox: Sx = { width: 260, pt: 2 };

export const closeRow: Sx = {
  display: 'flex',
  justifyContent: 'flex-end',
  px: 1,
};
