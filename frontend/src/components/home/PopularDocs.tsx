import { Box, Typography, Grid } from '@mui/material';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { getPopularDocuments } from '@/api/endpoints';
import DocumentCard from '@/components/common/DocumentCard';
import DocumentCardSkeleton from '@/components/common/DocumentCardSkeleton';
import GlassCard from '@/components/ui/GlassCard';
import AdBanner from '@/components/ui/AdBanner';
import * as s from './PopularDocs.styles';

export default function PopularDocs() {
  const { t } = useTranslation();
  const { data: docs, isLoading } = useQuery({ queryKey: ['popular-docs'], queryFn: getPopularDocuments });

  const hasDocs = !isLoading && (docs?.length ?? 0) > 0;
  const topDocs = hasDocs ? docs!.slice(0, 4) : [];
  const maxDl = Math.max(...topDocs.map((d) => d.downloadCount), 1);

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={s.section}>
        <Box sx={s.header}>
          <Typography variant="h5" sx={s.title}>
            <span role="img" aria-label="">📚</span> {t('popular.title')}
          </Typography>
          <Box component={Link} to="/browse" sx={s.viewAllLink}>
            {t('popular.viewAll')} →
          </Box>
        </Box>

        <Box sx={s.row}>
          <Box sx={s.docsCol}>
            {isLoading ? (
              <Grid container spacing={2}>
                {Array.from({ length: 4 }).map((_, i) => (
                  <Grid key={i} size={{ xs: 12, sm: 6 }}>
                    <DocumentCardSkeleton />
                  </Grid>
                ))}
              </Grid>
            ) : hasDocs ? (
              <Grid container spacing={2}>
                {topDocs.map((doc, idx) => (
                  <Grid key={doc.id} size={{ xs: 12, sm: 6 }}>
                    <DocumentCard
                      document={doc}
                      haloStrength={idx === 0 ? Math.min(doc.downloadCount / maxDl, 1) : 0}
                    />
                  </Grid>
                ))}
              </Grid>
            ) : (
              <GlassCard sx={s.emptyCard}>
                <Typography sx={s.emptyIcon} role="img" aria-label="">✨</Typography>
                <Typography variant="body1" color="text.secondary" sx={s.emptyText}>
                  {t('popular.empty')}
                </Typography>
              </GlassCard>
            )}
          </Box>

          <Box sx={s.sidebarCol}>
            <AdBanner width={300} height={250} />
          </Box>
        </Box>
      </Box>
    </motion.section>
  );
}
