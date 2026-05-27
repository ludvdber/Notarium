import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Typography, Box, Button, Chip, TextField, Grid, Snackbar, Alert } from '@mui/material';
import { Download, Favorite, FavoriteBorder, Flag, Share, Verified, SmartToy } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  getDocumentById,
  rateDocument,
  toggleFavorite,
  reportDocument,
  getAverageRating,
  recordDocVisit,
  getFavoriteStatus,
} from '@/api/endpoints';
import { Helmet } from 'react-helmet-async';
import { useAuthStore } from '@/stores/useAuthStore';
import { categoryColor, formatDate, shareOrCopy } from '@/lib/utils';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import StarRating from '@/components/ui/StarRating';
import Shimmer from '@/components/ui/Shimmer';
import AdSlot from '@/components/ui/AdSlot';
import * as s from './DocumentView.styles';

export default function DocumentView() {
  const { id } = useParams<{ id: string }>();
  const { t, i18n } = useTranslation();
  const { token, isVerified } = useAuthStore();
  const queryClient = useQueryClient();
  const [reportReason, setReportReason] = useState('');
  const [showReport, setShowReport] = useState(false);
  const [isFav, setIsFav] = useState(false);
  const [shareStatus, setShareStatus] = useState<'copied' | 'shared' | null>(null);

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

  // Hydrate the heart icon at load — without this, the button always says "Add to favorites"
  // even when the doc is already in the user's favorites.
  const { data: favStatus } = useQuery({
    queryKey: ['favorite-status', id],
    queryFn: () => getFavoriteStatus(Number(id)),
    enabled: !!id && !!token,
  });

  // Sync the heart icon with the server's favorite status when it loads/changes. Adjusting
  // state during render (React's recommended pattern) instead of an effect avoids a cascading render.
  const [prevFavStatus, setPrevFavStatus] = useState(favStatus);
  if (favStatus !== prevFavStatus) {
    setPrevFavStatus(favStatus);
    if (favStatus) setIsFav(favStatus.isFavorite);
  }

  // Iframe pulls the PDF directly from the authenticated endpoint — same-origin, browser sends the
  // HttpOnly JWT cookie automatically. Avoids the blob/URL.createObjectURL dance which raced with
  // React StrictMode's double-mount cleanup and left the iframe pointing at a revoked URL on first paint.
  const pdfSrc = isVerified && doc ? `/api/documents/${id}/file` : null;

  // Record visit so this doc surfaces in the user's "recent" trail on the home page
  useEffect(() => {
    if (!token || !doc?.id || !doc.verified) return;
    recordDocVisit(doc.id).catch(() => { /* best-effort, no UX impact */ });
    queryClient.invalidateQueries({ queryKey: ['recent-docs'] });
  }, [token, doc?.id, doc?.verified, queryClient]);

  const rateMutation = useMutation({
    mutationFn: (score: number) => rateDocument(Number(id), { score }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['rating', id] }),
  });

  const favMutation = useMutation({
    mutationFn: () => toggleFavorite(Number(id)),
    onSuccess: (data) => {
      setIsFav(data.isFavorite);
      queryClient.setQueryData(['favorite-status', id], data);
      queryClient.invalidateQueries({ queryKey: ['my-favorites'] });
    },
  });

  const reportMutation = useMutation({
    mutationFn: () => reportDocument(Number(id), { reason: reportReason }),
    onSuccess: () => {
      setShowReport(false);
      setReportReason('');
    },
  });

  const handleDownload = () => {
    if (!pdfSrc) return;
    const a = document.createElement('a');
    a.href = pdfSrc;
    a.download = `${doc?.title ?? 'document'}.pdf`;
    a.click();
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
    <PageWrapper maxWidth="lg">
      <Helmet><title>{doc ? `${doc.title} — Freenote` : 'Freenote'}</title></Helmet>
      <Box sx={s.header}>
        <Box sx={s.chipsRow}>
          <Chip
            size="small"
            label={t(`categories.${doc.category}`)}
            sx={s.categoryChip(categoryColor(doc.category))}
          />
          {doc.verified && (
            <Chip
              size="small"
              variant="outlined"
              color="primary"
              icon={<Verified sx={{ fontSize: 14 }} />}
              label={t('document.verified')}
            />
          )}
          {doc.aiGenerated && (
            <Chip
              size="small"
              variant="outlined"
              color="warning"
              icon={<SmartToy sx={{ fontSize: 14 }} />}
              label={t('document.aiGenerated')}
            />
          )}
        </Box>

        <Typography variant="h3" sx={s.title}>
          {doc.title}
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={s.subtitle}>
          {doc.courseName} — {doc.sectionName} — {doc.authorName}
        </Typography>
      </Box>

      {/* PDF Viewer */}
      {isVerified && pdfSrc && (
        <Box sx={s.pdfViewerWrapper}>
          <Box
            component="iframe"
            src={pdfSrc}
            sx={s.pdfIframe}
            title={doc.title}
          />
        </Box>
      )}

      {!isVerified && token && (
        <GlassCard sx={{ p: 3, mb: 3, textAlign: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            {t('auth.verifyEmailMessage')}
          </Typography>
        </GlassCard>
      )}

      <GlassCard sx={s.metaCard}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.language')}
            </Typography>
            <Typography variant="body2">{doc.language}</Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.year')}
            </Typography>
            <Typography variant="body2">{doc.year ?? '—'}</Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.professor')}
            </Typography>
            <Typography variant="body2">{doc.professorName ?? '—'}</Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.downloads')}
            </Typography>
            <Typography variant="body2" className="mono">
              {doc.downloadCount}
            </Typography>
          </Grid>
          <Grid size={{ xs: 6, sm: 4, md: 2.4 }}>
            <Typography variant="caption" color="text.secondary">
              {t('document.publishedAt')}
            </Typography>
            <Typography variant="body2" className="mono">
              {formatDate(doc.createdAt, i18n.language)}
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
        {isVerified && pdfSrc && (
          <Button variant="contained" startIcon={<Download />} onClick={handleDownload}>
            {t('document.download')}
          </Button>
        )}
        <Button
          variant="outlined"
          startIcon={<Share />}
          onClick={async () => {
            const result = await shareOrCopy({
              title: doc?.title,
              text: doc?.title,
              url: window.location.href,
            });
            if (result !== 'error') setShareStatus(result);
          }}
        >
          {t('common.share')}
        </Button>
        {token && (
          <Button
            variant="outlined"
            color={isFav ? 'error' : 'primary'}
            startIcon={isFav ? <Favorite /> : <FavoriteBorder />}
            onClick={() => favMutation.mutate()}
          >
            {isFav ? t('document.removeFavorite') : t('document.addFavorite')}
          </Button>
        )}
        {isVerified && (
          <Button variant="outlined" color="error" startIcon={<Flag />} onClick={() => setShowReport(!showReport)}>
            {t('document.report')}
          </Button>
        )}
      </Box>

      <Snackbar open={shareStatus !== null} autoHideDuration={2000} onClose={() => setShareStatus(null)}>
        <Alert severity="success" onClose={() => setShareStatus(null)}>
          {shareStatus === 'shared' ? t('common.shared') : t('common.linkCopied')}
        </Alert>
      </Snackbar>

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

      <AdSlot width={728} height={90} sx={{ mt: 4 }} />
    </PageWrapper>
  );
}
