import { Box, Typography, Grid, Skeleton } from '@mui/material';
import { TrendingUp } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { getStats } from '@/api/endpoints';
import { formatNumber } from '@/lib/utils';
import GlassCard from '@/components/ui/GlassCard';
import { buildStatItems } from './StatsSection.data';
import * as s from './StatsSection.styles';

function StatShimmer() {
  return (
    <Grid container spacing={2}>
      {Array.from({ length: 4 }).map((_, i) => (
        <Grid key={i} size={{ xs: 6, md: 3 }}>
          <Skeleton variant="rounded" height={110} sx={s.shimmerItem} />
        </Grid>
      ))}
    </Grid>
  );
}

export default function StatsSection() {
  const { t } = useTranslation();
  const { data: stats, isLoading } = useQuery({ queryKey: ['stats'], queryFn: getStats });

  const items = stats ? buildStatItems(stats, t) : [];

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={s.section}>
        {isLoading ? (
          <StatShimmer />
        ) : (
          <>
            <Grid container spacing={2}>
              {items.map((item, i) => (
                <Grid key={item.label} size={{ xs: 6, md: 3 }}>
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    whileInView={{ opacity: 1, y: 0 }}
                    transition={{ delay: i * 0.08 }}
                    viewport={{ once: true }}
                  >
                    <GlassCard sx={s.card}>
                      <Typography sx={s.icon}>{item.icon}</Typography>
                      <Typography variant="h3" className="mono" sx={s.value(item.color)}>
                        {formatNumber(item.value)}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" sx={s.label}>
                        {item.label}
                      </Typography>
                    </GlassCard>
                  </motion.div>
                </Grid>
              ))}
            </Grid>

            <Box sx={s.weekBadgeWrapper}>
              <Box sx={s.weekBadge}>
                <TrendingUp sx={s.weekBadgeIcon} />
                <Typography variant="body2" sx={s.weekBadgeCount} className="mono">
                  +{stats?.weekUploads ?? 0}
                </Typography>
                <Typography variant="body2" sx={s.weekBadgeLabel}>
                  {t('stats.weekUploads')}
                </Typography>
              </Box>
            </Box>
          </>
        )}
      </Box>
    </motion.section>
  );
}
