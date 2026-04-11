import { useState } from 'react';
import { Box, Typography, Grid, Chip, Avatar } from '@mui/material';
import { EmojiEvents, FavoriteBorder } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import { getLeaderboard, getDelegates } from '@/api/endpoints';
import GlassCard from '@/components/ui/GlassCard';
import Badge from '@/components/ui/Badge';
import { RANK_COLORS } from './LeaderboardDelegates.data';
import * as s from './LeaderboardDelegates.styles';

export default function LeaderboardDelegates() {
  const { t } = useTranslation();
  const { data: leaderboard } = useQuery({ queryKey: ['leaderboard'], queryFn: getLeaderboard });
  const { data: delegates } = useQuery({ queryKey: ['delegates'], queryFn: getDelegates });
  const [selectedDelegate, setSelectedDelegate] = useState<{ username: string; discord: string | null } | null>(null);

  const hasLeaderboard = (leaderboard?.length ?? 0) > 0;
  const hasDelegates = (delegates?.length ?? 0) > 0;

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Grid container spacing={3} sx={s.outerGrid}>
        {/* Leaderboard */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Typography variant="h5" sx={s.columnTitle}>
            🏆 {t('leaderboard.title')}
          </Typography>
          <GlassCard sx={s.listCard}>
            {!hasLeaderboard && (
              <Box sx={s.emptyState}>
                <Typography sx={s.emptyIcon}>🏅</Typography>
                <Typography variant="body2" color="text.secondary">
                  {t('leaderboard.empty')}
                </Typography>
              </Box>
            )}
            {leaderboard?.slice(0, 3).map((entry) => {
              const rankColor = RANK_COLORS[entry.rank - 1] ?? '#888';
              return (
                <Box key={entry.rank} sx={s.entryRow}>
                  <Box sx={s.medalCircle(entry.rank, rankColor)}>
                    {entry.rank <= 3 ? (
                      <EmojiEvents sx={s.medalIcon(rankColor)} />
                    ) : (
                      <Typography className="mono" sx={s.medalRank}>
                        #{entry.rank}
                      </Typography>
                    )}
                  </Box>

                  <Box sx={s.userInfo}>
                    <Box sx={s.userNameRow}>
                      <Typography variant="body2" sx={s.userName}>
                        {entry.username}
                      </Typography>
                      {entry.supporter && (
                        <Chip
                          icon={<FavoriteBorder sx={s.supporterIcon} />}
                          label={t('document.supporter')}
                          size="small"
                          color="secondary"
                          variant="outlined"
                          sx={s.supporterChip}
                        />
                      )}
                    </Box>
                    {entry.badges.length > 0 && (
                      <Box sx={s.badgesRow}>
                        {entry.badges.slice(0, 3).map((b) => (
                          <Badge key={b} label={b} />
                        ))}
                      </Box>
                    )}
                  </Box>

                  <Box sx={s.xpCol}>
                    <Typography className="mono" sx={s.xpValue}>
                      {entry.xp}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      XP
                    </Typography>
                  </Box>
                </Box>
              );
            })}
          </GlassCard>
        </Grid>

        {/* Delegates */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Typography variant="h5" sx={s.columnTitle}>
            🎖️ {t('delegates.title')}
          </Typography>
          <GlassCard sx={s.delegatesCard}>
            {!hasDelegates && (
              <Box sx={s.emptyState}>
                <Typography sx={s.emptyIcon}>📋</Typography>
                <Typography variant="body2" color="text.secondary">
                  {t('delegates.empty')}
                </Typography>
              </Box>
            )}
            {delegates?.map((delegate) => (
              <Box key={delegate.sectionName} sx={s.delegateBlock}>
                <Typography variant="subtitle2" color="text.secondary" sx={s.delegateSectionName}>
                  {delegate.sectionName}
                </Typography>
                <Box sx={s.delegateMembers}>
                  {delegate.members.map((m) => (
                    <Chip
                      key={m.username}
                      avatar={<Avatar sx={{ width: 24, height: 24 }}>{m.username.charAt(0).toUpperCase()}</Avatar>}
                      label={m.username}
                      variant="outlined"
                      size="small"
                      onClick={() => setSelectedDelegate(selectedDelegate?.username === m.username ? null : m)}
                      sx={s.delegateChip}
                    />
                  ))}
                </Box>
              </Box>
            ))}

            <AnimatePresence>
              {selectedDelegate?.discord && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  style={s.discordPopup}
                >
                  <Box sx={s.discordBox}>
                    <Typography variant="body2" sx={s.discordName}>
                      {selectedDelegate.username}
                    </Typography>
                    <Typography variant="caption" sx={s.discordHandle}>
                      {selectedDelegate.discord}
                    </Typography>
                  </Box>
                </motion.div>
              )}
            </AnimatePresence>
          </GlassCard>
        </Grid>
      </Grid>
    </motion.section>
  );
}
