import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const title: Sx = { fontWeight: 700, mb: 3 };

export const rankCell = (isTopThree: boolean): Sx => ({
  fontWeight: 700,
  color: isTopThree ? 'primary.main' : 'inherit',
});

export const badgesCell: Sx = {
  display: 'flex',
  gap: 0.5,
  flexWrap: 'wrap',
};
