import { useState } from 'react';
import { Box } from '@mui/material';
import { Star, StarBorder, StarHalf } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { TOKENS } from '@/theme/tokens';

interface StarRatingProps {
  value: number | null;
  onChange?: (value: number) => void;
  readOnly?: boolean;
  /** Visual size — defaults to 28 in interactive mode (more tappable), 20 in read-only. */
  size?: number;
}

export default function StarRating({ value, onChange, readOnly = false, size }: StarRatingProps) {
  const { t } = useTranslation();
  const [hover, setHover] = useState<number | null>(null);
  const rating = value ?? 0;
  const displayed = !readOnly && hover !== null ? hover : rating;
  const iconSize = size ?? (readOnly ? 20 : 28);

  return (
    <Box
      sx={{ display: 'inline-flex', alignItems: 'center', gap: 0.25 }}
      role="group"
      aria-label={t('document.rating')}
      onMouseLeave={() => !readOnly && setHover(null)}
    >
      {[1, 2, 3, 4, 5].map((star) => {
        const Icon =
          displayed >= star ? Star : displayed >= star - 0.5 ? StarHalf : StarBorder;
        const isFilled = displayed >= star - 0.5;
        const isHoverPreview = !readOnly && hover !== null && hover >= star;

        return (
          <Box
            key={star}
            component="span"
            role={readOnly ? undefined : 'button'}
            tabIndex={readOnly ? -1 : 0}
            aria-label={`${star}/5`}
            aria-pressed={!readOnly && rating === star}
            onClick={() => !readOnly && onChange?.(star)}
            onMouseEnter={() => !readOnly && setHover(star)}
            onKeyDown={(e) => {
              if (readOnly) return;
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault();
                onChange?.(star);
              }
            }}
            sx={{
              display: 'inline-flex',
              cursor: readOnly ? 'default' : 'pointer',
              transition: 'transform 0.12s',
              transformOrigin: 'center',
              '&:hover': readOnly ? undefined : { transform: 'scale(1.18)' },
              '&:focus-visible': {
                outline: (t) => `2px solid ${t.palette.primary.main}`,
                outlineOffset: 2,
                borderRadius: 0.5,
              },
            }}
          >
            <Icon
              sx={{
                fontSize: iconSize,
                color: isFilled ? TOKENS.rating.main : 'text.disabled',
                opacity: isHoverPreview && !isFilled ? 0.6 : 1,
                filter: isFilled ? `drop-shadow(0 0 4px ${TOKENS.rating.main}66)` : 'none',
                transition: 'color 0.12s, opacity 0.12s, filter 0.12s',
              }}
            />
          </Box>
        );
      })}
    </Box>
  );
}
