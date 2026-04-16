import type { SxProps, Theme } from '@mui/material';

type Sx = SxProps<Theme>;

export const section: Sx = { py: { xs: 6, md: 9 } };

export const flexRow: Sx = {
  display: 'flex',
  flexDirection: { xs: 'column', md: 'row' },
  alignItems: 'stretch',
  gap: 3,
};

export const newsCol: Sx = {
  flex: { xs: '1 1 auto', md: '7 1 0' },
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
};

export const linksCol: Sx = {
  flex: { xs: '1 1 auto', md: '5 1 0' },
  minWidth: 0,
  display: 'flex',
  flexDirection: 'column',
};

export const columnTitle: Sx = { mb: 2, fontWeight: 800 };

export const newsCard: Sx = {
  flex: 1,
  display: 'flex',
  flexDirection: 'column',
};

export const newsList: Sx = {
  flex: 1,
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'space-around',
  py: 1,
};

export const newsItem: Sx = {
  py: 1,
  borderRadius: 1.5,
  transition: 'background-color 0.2s',
  '&:visited .MuiTypography-root': { color: 'text.secondary' },
};

export const newsEmptyWrapper: Sx = {
  py: 4,
  textAlign: 'center',
  color: 'text.secondary',
};

export const externalHint: Sx = {
  fontSize: 11,
  opacity: 0.5,
  display: 'flex',
  alignItems: 'center',
};

export const newsTitle: Sx = { fontWeight: 600, lineHeight: 1.3 };

export const newsLabelsRow: Sx = { display: 'flex', gap: 0.5, ml: 1, flexShrink: 0 };

export const newsLabelChip: Sx = { fontSize: 10, height: 20 };

export const linksCard: Sx = {
  flex: 1,
  p: 2,
  display: 'flex',
  flexDirection: 'column',
  gap: 2,
};

export const mainLinksCol: Sx = {
  display: 'flex',
  flexDirection: 'column',
  gap: 1,
  flex: 1,
};

export const mainLinkRow = (color: string): Sx => ({
  display: 'flex',
  alignItems: 'center',
  gap: 1.5,
  p: 1.5,
  borderRadius: 2,
  bgcolor: `${color}10`,
  border: `1px solid ${color}25`,
  textDecoration: 'none',
  color: 'inherit',
  transition: 'transform 0.15s',
  flex: 1,
  '&:hover': { transform: 'translateX(4px)' },
});

export const mainLinkIcon = (color: string): SxProps<Theme> => ({
  color,
  display: 'flex',
});

export const mainLinkLabel: Sx = { fontWeight: 700 };

export const secondaryGrid: Sx = {
  display: 'grid',
  gridTemplateColumns: '1fr 1fr',
  gap: 1,
};

export const secondaryLinkRow: Sx = {
  display: 'flex',
  alignItems: 'center',
  gap: 1,
  p: 1,
  borderRadius: 1.5,
  textDecoration: 'none',
  color: 'text.secondary',
  '&:hover': { color: 'primary.main' },
};

export const secondaryLinkLabel: Sx = { fontWeight: 600 };
