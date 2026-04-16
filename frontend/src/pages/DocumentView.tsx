import { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { Typography, Box, Button, Chip, TextField, Grid, Snackbar, Alert } from '@mui/material';
import { Download, Favorite, FavoriteBorder, Flag, ContentCopy } from '@mui/icons-material';
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
import { Helmet } from 'react-helmet-async';
import { useAuthStore } from '@/stores/useAuthStore';
import { categoryColor, formatDate } from '@/lib/utils';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import StarRating from '@/components/ui/StarRating';
import Shimmer from '@/components/ui/Shimmer';
import AdBanner from '@/components/ui/AdBanner';
import * as s from './DocumentView.styles';

export default function DocumentView() {
  const { id } = useParams<{ id: string }>();
  const { t, i18n } = useTranslation();
  const { token, isVerified } = useAuthStore();
  const queryClient = useQueryClient();
  const [reportReason, setReportReason] = useState('');
  const [showReport, setShowReport] = useState(false);
  const [isFav, setIsFav] = useState(false);
  const [linkCopied, setLinkCopied] = useState(false);
  const pdfUrlRef = useRef<string | null>(null);

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

  // Auto-load PDF for verified users via useQuery
  const { data: pdfUrl, isLoading: pdfLoading } = useQuery({
    queryKey: ['pdf-blob', id],
    queryFn: async () => {
      const blob = await downloadDocument(Number(id));
      return URL.createObjectURL(blob);
    },
    enabled: isVerified && !!doc,
    staleTime: Infinity,
  });

  // Cleanup blob URL on unmount
  useEffect(() => {
    if (pdfUrl) pdfUrlRef.current = pdfUrl;
    return () => {
      if (pdfUrlRef.current) URL.revokeObjectURL(pdfUrlRef.current);
    };
  }, [pdfUrl]);

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

  const handleDownload = () => {
    if (pdfUrl) {
      const a = document.createElement('a');
      a.href = pdfUrl;
      a.download = `${doc?.title ?? 'document'}.pdf`;
      a.click();
    }
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

      {/* PDF Viewer */}
      {isVerified && (
        <Box sx={s.pdfViewerWrapper}>
          {pdfLoading && (
            <Box sx={s.pdfLoading}>
              <Typography color="text.secondary">{t('common.loading')}</Typography>
            </Box>
          )}
          {pdfUrl && (
            <Box
              component="iframe"
              src={pdfUrl}
              sx={s.pdfIframe}
              title={doc.title}
            />
          )}
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
        {isVerified && pdfUrl && (
          <Button variant="contained" startIcon={<Download />} onClick={handleDownload}>
            {t('document.download')}
          </Button>
        )}
        <Button
          variant="outlined"
          startIcon={<ContentCopy />}
          onClick={() => {
            navigator.clipboard.writeText(window.location.href);
            setLinkCopied(true);
          }}
        >
          {t('common.copyLink')}
        </Button>
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

      <Snackbar open={linkCopied} autoHideDuration={2000} onClose={() => setLinkCopied(false)}>
        <Alert severity="success" onClose={() => setLinkCopied(false)}>
          {t('common.linkCopied')}
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

      <Typography variant="caption" color="text.secondary" sx={s.createdAt}>
        {formatDate(doc.createdAt, i18n.language)}
      </Typography>

      <Box sx={{ mt: 4 }}>
        <AdBanner width={728} height={90} />
      </Box>
    </PageWrapper>
  );
}
