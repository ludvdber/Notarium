import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Typography, Box, Button, Chip, TextField, Grid } from '@mui/material';
import { Download, Favorite, FavoriteBorder, Flag } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  getDocumentById,
  downloadDocument,
  rateDocument,
  toggleFavorite,
  reportDocument,
  getAverageRating,
} from '@/api/endpoints';
import { useAuthStore } from '@/stores/useAuthStore';
import { categoryColor, formatDate } from '@/lib/utils';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import StarRating from '@/components/ui/StarRating';
import Shimmer from '@/components/ui/Shimmer';
import * as s from './DocumentView.styles';

export default function DocumentView() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const { token, isVerified } = useAuthStore();
  const queryClient = useQueryClient();
  const [reportReason, setReportReason] = useState('');
  const [showReport, setShowReport] = useState(false);
  const [isFav, setIsFav] = useState(false);

  const { data: doc, isLoading } = useQuery({
    queryKey: ['document', id],
    queryFn: () => getDocumentById(Number(id)),
    enabled: !!id,
  });

  const { data: avgRating } = useQuery({
    queryKey: ['rating', id],
    queryFn: () => getAverageRating(Number(id)),
    enabled: !!id,
  });

  const rateMutation = useMutation({
    mutationFn: (score: number) => rateDocument(Number(id), { score }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rating', id] }),
  });

  const favMutation = useMutation({
    mutationFn: () => toggleFavorite(Number(id)),
    onSuccess: (data) => setIsFav(data.isFavorite),
  });

  const reportMutation = useMutation({
    mutationFn: () => reportDocument(Number(id), { reason: reportReason }),
    onSuccess: () => {
      setShowReport(false);
      setReportReason('');
    },
  });

  const handleDownload = async () => {
    const blob = await downloadDocument(Number(id));
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${doc?.title ?? 'document'}.pdf`;
    a.click();
    URL.revokeObjectURL(url);
  };

  if (isLoading) {
    return (
      <PageWrapper>
        <Shimmer count={3} height={200} />
      </PageWrapper>
    );
  }
  if (!doc) {
    return (
      <PageWrapper>
        <Typography>{t('common.error')}</Typography>
      </PageWrapper>
    );
  }

  return (
    <PageWrapper maxWidth="md">
      <Box sx={s.header}>
        <Box sx={s.chipsRow}>
          <Chip label={t(`categories.${doc.category}`)} sx={s.categoryChip(categoryColor(doc.category))} />
          {doc.verified && <Chip label={t('document.verified')} color="primary" size="small" />}
          {doc.aiGenerated && <Chip label={t('document.aiGenerated')} color="warning" size="small" />}
        </Box>

        <Typography variant="h3" sx={s.title}>
          {doc.title}
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={s.subtitle}>
          {doc.courseName} — {doc.sectionName} — {doc.authorName}
        </Typography>
      </Box>

      <GlassCard sx={s.metaCard}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 6, sm: 3 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.language')}
            </Typography>
            <Typography variant="body2">{doc.language}</Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 3 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.year')}
            </Typography>
            <Typography variant="body2">{doc.year ?? '—'}</Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 3 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.professor')}
            </Typography>
            <Typography variant="body2">{doc.professorName ?? '—'}</Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 3 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.downloads')}
            </Typography>
            <Typography variant="body2" className="mono">
              {doc.downloadCount}
            </Typography>
          </Grid>
        </Grid>
      </GlassCard>

      {doc.tags.length > 0 && (
        <Box sx={s.tagsRow}>
          {doc.tags.map((tag) => (
            <Chip key={tag} label={tag} size="small" variant="outlined" />
          ))}
        </Box>
      )}

      {doc.summaryAi && (
        <GlassCard sx={s.summaryCard}>
          <Typography variant="subtitle2" color="text.secondary" sx={s.summaryLabel}>
            {t('document.summary')}
          </Typography>
          <Typography variant="body2">{doc.summaryAi}</Typography>
        </GlassCard>
      )}

      <Box sx={s.ratingRow}>
        <Box sx={s.ratingInner}>
          <Typography variant="body2" color="text.secondary">
            {t('document.rating')}:
          </Typography>
          <StarRating
            value={avgRating ?? doc.averageRating}
            readOnly={!isVerified}
            onChange={(v) => rateMutation.mutate(v)}
          />
        </Box>
      </Box>

      <Box sx={s.actionsRow}>
        {isVerified && (
          <Button variant="contained" startIcon={<Download />} onClick={handleDownload}>
            {t('document.download')}
          </Button>
        )}
        {token && (
          <Button
            variant="outlined"
            startIcon={isFav ? <Favorite /> : <FavoriteBorder />}
            onClick={() => favMutation.mutate()}
          >
            {isFav ? t('document.favorite') : t('document.addFavorite')}
          </Button>
        )}
        {isVerified && (
          <Button variant="outlined" color="error" startIcon={<Flag />} onClick={() => setShowReport(!showReport)}>
            {t('document.report')}
          </Button>
        )}
      </Box>

      {showReport && (
        <Box sx={s.reportRow}>
          <TextField
            size="small"
            fullWidth
            value={reportReason}
            onChange={(e) => setReportReason(e.target.value)}
            placeholder={t('document.reportPlaceholder')}
          />
          <Button
            variant="contained"
            color="error"
            onClick={() => reportMutation.mutate()}
            disabled={!reportReason}
          >
            {t('common.confirm')}
          </Button>
        </Box>
      )}

      <Typography variant="caption" color="text.secondary" sx={s.createdAt}>
        {formatDate(doc.createdAt)}
      </Typography>
    </PageWrapper>
  );
}
