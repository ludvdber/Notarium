import { Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Box } from '@mui/material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { getLeaderboard } from '@/api/endpoints';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';
import Badge from '@/components/ui/Badge';
import * as s from './Leaderboard.styles';

export default function Leaderboard() {
  const { t } = useTranslation();
  const { data: entries } = useQuery({ queryKey: ['leaderboard'], queryFn: getLeaderboard });

  return (
    <PageWrapper>
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
    </PageWrapper>
  );
}
