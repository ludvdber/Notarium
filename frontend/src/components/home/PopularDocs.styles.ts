import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const section: Sx = { py: { xs: 6, md: 9 } };

export const header: Sx = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'center',
  mb: 3,
};

export const title: Sx = { fontWeight: 800 };

export const viewAllLink: Sx = {
  color: '#00d2ff',
  textDecoration: 'none',
  fontSize: 13,
  fontWeight: 700,
  '&:hover': { textDecoration: 'underline' },
};

export const row: Sx = {
  display: 'flex',
  gap: 3,
  flexDirection: { xs: 'column', lg: 'row' },
};

export const docsCol: Sx = { flex: 1 };

export const sidebarCol: Sx = {
  width: { xs: '100%', lg: 300 },
  flexShrink: 0,
  display: 'flex',
  alignItems: 'flex-start',
  justifyContent: 'center',
};

export const emptyCard: Sx = { p: 5, textAlign: 'center' };

export const emptyIcon: Sx = { fontSize: 40, mb: 1 };

export const emptyText: Sx = { fontWeight: 600 };
