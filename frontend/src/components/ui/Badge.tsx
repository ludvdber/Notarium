import { Chip } from '@mui/material';
import { EmojiEvents } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { TOKENS } from '@/theme/tokens';

interface BadgeProps {
  label: string;
}

const BADGE_COLORS: Record<string, string> = {
  FIRST_UPLOAD: TOKENS.rating.main,
  CONTRIBUTOR_10: TOKENS.sections.assistant,
  CONTRIBUTOR_50: TOKENS.sections.informatique,
  CONTRIBUTOR_100: TOKENS.sections.comptabilite,
  XP_100: TOKENS.sections.fiscalite,
  XP_500: TOKENS.categories.EXAMEN,
  XP_1000: TOKENS.sections.marketing,
};

export default function Badge({ label }: BadgeProps) {
  const { t } = useTranslation();
  const color = BADGE_COLORS[label] ?? '#888';
  const displayName = t(`badges.${label}`, { defaultValue: label.replace('_', ' ') });
  return (
    <Chip
      icon={<EmojiEvents sx={{ fontSize: 16, color: `${color} !important` }} />}
      label={displayName}
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
