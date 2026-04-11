import { Chip } from '@mui/material';
import { EmojiEvents } from '@mui/icons-material';

interface BadgeProps {
  label: string;
}

const BADGE_COLORS: Record<string, string> = {
  FIRST_UPLOAD: '#ffd93d',
  CONTRIBUTOR_10: '#6bcb77',
  CONTRIBUTOR_50: '#00d2ff',
  CONTRIBUTOR_100: '#7b2ff7',
  XP_100: '#ff9a3c',
  XP_500: '#ff6b6b',
  XP_1000: '#ff2e63',
};

export default function Badge({ label }: BadgeProps) {
  const color = BADGE_COLORS[label] ?? '#888';
  return (
    <Chip
      icon={<EmojiEvents sx={{ fontSize: 16, color: `${color} !important` }} />}
      label={label.replace('_', ' ')}
      size="small"
      sx={{
        borderColor: color,
        color,
        fontWeight: 600,
        fontSize: 11,
      }}
      variant="outlined"
    />
  );
}
