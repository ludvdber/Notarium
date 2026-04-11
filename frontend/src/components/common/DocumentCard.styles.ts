import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const card: Sx = {
  display: 'block',
  textDecoration: 'none',
  height: '100%',
};

export const content: Sx = { p: 2, '&:last-child': { pb: 2 } };

export const headerRow: Sx = { display: 'flex', gap: 1.5, mb: 1.5 };

export const pdfIcon: Sx = {
  fontSize: 32,
  color: '#ef4444',
  flexShrink: 0,
  mt: 0.25,
};

export const headerText: Sx = { flex: 1, minWidth: 0 };

export const title: Sx = { fontWeight: 700, lineHeight: 1.3, mb: 0.5 };

export const badgesRow: Sx = {
  display: 'flex',
  gap: 0.5,
  mb: 1.5,
  flexWrap: 'wrap',
};

export const categoryChip = (color: string): Sx => ({
  bgcolor: `${color}15`,
  color,
  fontWeight: 600,
  fontSize: 10,
  height: 22,
});

export const badgeChip: Sx = { fontSize: 10, height: 22 };

export const badgeIcon: Sx = { fontSize: '14px !important' };

export const footerRow: Sx = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
};

export const ratingStyle: Sx = {
  '& .MuiRating-iconFilled': { color: '#ffd93d' },
};

export const downloadsRow: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 0.5,
};

export const downloadsIcon: Sx = { fontSize: 14, color: 'text.secondary' };

export const tagsRow: Sx = {
  display: 'flex',
  gap: 0.5,
  mt: 1,
  flexWrap: 'wrap',
};

export const tagChip: Sx = { fontSize: 10, height: 20 };

export const createdAt: Sx = {
  mt: 1,
  display: 'block',
  fontSize: 10,
};
