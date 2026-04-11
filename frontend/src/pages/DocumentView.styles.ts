import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const header: Sx = { mb: 3 };

export const chipsRow: Sx = {
  display: 'flex',
  gap: 1,
  mb: 2,
  flexWrap: 'wrap',
};

export const categoryChip = (color: string): Sx => ({
  bgcolor: `${color}20`,
  color,
});

export const title: Sx = { fontWeight: 800, mb: 1 };

export const subtitle: Sx = { mb: 2 };

export const metaCard: Sx = { p: 3, mb: 3 };

export const tagsRow: Sx = {
  display: 'flex',
  gap: 0.5,
  mb: 3,
  flexWrap: 'wrap',
};

export const summaryCard: Sx = { p: 3, mb: 3 };

export const summaryLabel: Sx = { mb: 1 };

export const ratingRow: Sx = {
  display: 'flex',
  gap: 2,
  mb: 3,
  flexWrap: 'wrap',
  alignItems: 'center',
};

export const ratingInner: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 1,
};

export const actionsRow: Sx = {
  display: 'flex',
  gap: 2,
  flexWrap: 'wrap',
};

export const reportRow: Sx = {
  mt: 2,
  display: 'flex',
  gap: 1,
};

export const createdAt: Sx = {
  mt: 3,
  display: 'block',
};
