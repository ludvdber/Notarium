import { lazy, Suspense } from 'react';
import { Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Box, Skeleton } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { getLeaderboard } from '@/api/endpoints';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import Badge from '@/components/ui/Badge';
import AdBanner from '@/components/ui/AdBanner';
import Divider from '@/components/ui/Divider';
import * as s from './Leaderboard.styles';

const CommunityCarousel = lazy(() => import('@/components/home/CommunityCarousel'));

export default function Leaderboard() {
  const { t } = useTranslation();
  const { data: entries } = useQuery({ queryKey: ['leaderboard'], queryFn: getLeaderboard });

  return (
    <PageWrapper>
      <Helmet><title>{t('nav.leaderboard')} — Freenote</title></Helmet>
      <Typography variant="h4" sx={s.title}>
        {t('leaderboard.title')}
      </Typography>
      <GlassCard>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>{t('leaderboard.rank')}</TableCell>
                <TableCell>{t('leaderboard.username')}</TableCell>
                <TableCell align="right">{t('leaderboard.xp')}</TableCell>
                <TableCell align="right">{t('leaderboard.docs')}</TableCell>
                <TableCell>{t('leaderboard.badges')}</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {entries?.map((entry) => (
                <TableRow key={entry.rank}>
                  <TableCell>
                    <Typography className="mono" sx={s.rankCell(entry.rank <= 3)}>
                      #{entry.rank}
                    </Typography>
                  </TableCell>
                  <TableCell>{entry.username}</TableCell>
                  <TableCell align="right" className="mono">
                    {entry.xp}
                  </TableCell>
                  <TableCell align="right" className="mono">
                    {entry.documentCount}
                  </TableCell>
                  <TableCell>
                    <Box sx={s.badgesCell}>
                      {entry.badges.map((b) => (
                        <Badge key={b} label={b} />
                      ))}
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </GlassCard>
      <Box sx={{ display: 'flex', justifyContent: 'center', my: 3 }}>
        <AdBanner width={728} height={90} />
      </Box>
      <Divider />
      <Suspense fallback={<Skeleton variant="rounded" height={200} sx={{ borderRadius: 3, mt: 2 }} />}>
        <CommunityCarousel />
      </Suspense>
    </PageWrapper>
  );
}
