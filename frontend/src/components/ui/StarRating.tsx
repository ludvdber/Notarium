import { Box } from '@mui/material';
import { Star, StarBorder, StarHalf } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { TOKENS } from '@/theme/tokens';

interface StarRatingProps {
  value: number | null;
  onChange?: (value: number) => void;
  readOnly?: boolean;
}

export default function StarRating({ value, onChange, readOnly = false }: StarRatingProps) {
  const { t } = useTranslation();
  const rating = value ?? 0;

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.25 }} role="group" aria-label={t('document.rating')}>
      {[1, 2, 3, 4, 5].map((star) => {
        const Icon =
          rating >= star ? Star : rating >= star - 0.5 ? StarHalf : StarBorder;

        return (
          <Icon
            key={star}
            role={readOnly ? undefined : 'button'}
            tabIndex={readOnly ? -1 : 0}
            aria-label={`${star}/5`}
            sx={{
              fontSize: 20,
              color: TOKENS.rating.main,
              cursor: readOnly ? 'default' : 'pointer',
            }}
            onClick={() => !readOnly && onChange?.(star)}
          />
        );
      })}
    </Box>
  );
}
