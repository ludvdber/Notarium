import { Link } from 'react-router-dom';
import { CardContent, Typography, Box, Chip, Rating, Tooltip } from '@mui/material';
import { Download, Verified, SmartToy, PictureAsPdf } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import type { DocumentResponse } from '@/types';
import GlassCard from '@/components/ui/GlassCard';
import { categoryColor, formatDate } from '@/lib/utils';
import * as s from './DocumentCard.styles';

interface Props {
  document: DocumentResponse;
  // 0 → no glow. 1 → maximum cyan halo. Use for popular/featured lists.
  haloStrength?: number;
}

export default function DocumentCard({ document: doc, haloStrength = 0 }: Props) {
  const { t, i18n } = useTranslation();

  return (
    <GlassCard component={Link} to={`/documents/${doc.id}`} sx={s.card(haloStrength)}>
      <CardContent sx={s.content}>
        <Box sx={s.headerRow}>
          <PictureAsPdf sx={s.pdfIcon} />
          <Box sx={s.headerText}>
            <Tooltip title={doc.title} enterDelay={400}>
              <Typography variant="subtitle2" sx={s.title} noWrap>
                {doc.title}
              </Typography>
            </Tooltip>
            <Typography variant="caption" color="text.secondary" noWrap>
              {doc.courseName} — {doc.authorName}
            </Typography>
          </Box>
        </Box>

        <Box sx={s.badgesRow}>
          <Chip
            label={t(`categories.${doc.category}`)}
            size="small"
            sx={s.categoryChip(categoryColor(doc.category))}
          />
          {doc.verified && (
            <Chip
              icon={<Verified sx={s.badgeIcon} />}
              label={t('document.verified')}
              size="small"
              color="primary"
              variant="outlined"
              sx={s.badgeChip}
            />
          )}
          {doc.aiGenerated && (
            <Chip
              icon={<SmartToy sx={s.badgeIcon} />}
              label={t('document.aiShort')}
              size="small"
              color="warning"
              variant="outlined"
              sx={s.badgeChip}
            />
          )}
        </Box>

        <Box sx={s.footerRow}>
          <Rating value={doc.averageRating ?? 0} precision={0.5} readOnly size="small" sx={s.ratingStyle} />
          <Box sx={s.downloadsRow}>
            <Download sx={s.downloadsIcon} />
            <Typography variant="caption" className="mono" color="text.secondary">
              {doc.downloadCount}
            </Typography>
          </Box>
        </Box>

        {doc.tags.length > 0 && (
          <Box sx={s.tagsRow}>
            {doc.tags.slice(0, 3).map((tag) => (
              <Chip key={tag} label={tag} size="small" variant="outlined" sx={s.tagChip} />
            ))}
          </Box>
        )}

        <Typography variant="caption" color="text.secondary" sx={s.createdAt}>
          {formatDate(doc.createdAt, i18n.language)}
        </Typography>
      </CardContent>
    </GlassCard>
  );
}
