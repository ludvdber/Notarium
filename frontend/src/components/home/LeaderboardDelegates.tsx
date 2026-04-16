import { useState, useEffect, useRef } from 'react';
import { Box, Typography, Grid, Chip, Avatar } from '@mui/material';
import { EmojiEvents, FavoriteBorder } from '@mui/icons-material';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import { getLeaderboard, getDelegates } from '@/api/endpoints';
import { useAuthStore } from '@/stores/useAuthStore';
import { formatNumber, formatDate } from '@/lib/utils';
import type { DelegateMember } from '@/types';
import GlassCard from '@/components/ui/GlassCard';
import { RANK_COLORS } from './LeaderboardDelegates.data';
import * as s from './LeaderboardDelegates.styles';

export default function LeaderboardDelegates() {
  const { t, i18n } = useTranslation();
  const { data: leaderboard } = useQuery({ queryKey: ['leaderboard'], queryFn: getLeaderboard });
  const { data: delegates } = useQuery({ queryKey: ['delegates'], queryFn: getDelegates });
  const { token } = useAuthStore();
  const [selectedDelegate, setSelectedDelegate] = useState<DelegateMember | null>(null);
  const delegatesCardRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!selectedDelegate) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setSelectedDelegate(null);
    };
    const onClickOutside = (e: MouseEvent) => {
      if (delegatesCardRef.current && !delegatesCardRef.current.contains(e.target as Node)) {
        setSelectedDelegate(null);
      }
    };
    document.addEventListener('keydown', onKey);
    document.addEventListener('mousedown', onClickOutside);
    return () => {
      document.removeEventListener('keydown', onKey);
      document.removeEventListener('mousedown', onClickOutside);
    };
  }, [selectedDelegate]);

  const hasLeaderboard = (leaderboard?.length ?? 0) > 0;
  const hasDelegates = (delegates?.length ?? 0) > 0;

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Grid container spacing={3} sx={s.outerGrid}>
        {/* Leaderboard */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Box sx={s.columnHeader}>
            <Typography variant="h5" sx={s.columnTitle}>
              <span role="img" aria-label="">🏆</span> {t('leaderboard.title')}
            </Typography>
            {hasLeaderboard && (
              <Box component={Link} to="/leaderboard" sx={s.viewAllLink}>
                {t('leaderboard.viewAll')} →
              </Box>
            )}
          </Box>
          <GlassCard sx={s.listCard}>
            <Box sx={s.scrollableList}>
            {!hasLeaderboard && (
              <Box sx={s.emptyState}>
                <Typography sx={s.emptyIcon} role="img" aria-label="">🏅</Typography>
                <Typography variant="body2" color="text.secondary">
                  {t('leaderboard.empty')}
                </Typography>
              </Box>
            )}
            {leaderboard?.map((entry) => {
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
                  </Box>

                  <Box sx={s.xpCol}>
                    <Typography className="mono" sx={s.xpValue}>
                      {formatNumber(entry.xp)}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      XP
                    </Typography>
                  </Box>
                </Box>
              );
            })}
            </Box>
          </GlassCard>
        </Grid>

        {/* Delegates */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Typography variant="h5" sx={s.columnTitle}>
            <span role="img" aria-label="">🎖️</span> {t('delegates.title')}
          </Typography>
          <Box ref={delegatesCardRef}>
          <GlassCard sx={s.delegatesCard}>
            {!hasDelegates && (
              <Box sx={s.emptyState}>
                <Typography sx={s.emptyIcon} role="img" aria-label="">📋</Typography>
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
                  {delegate.members.map((m) => {
                    const label = m.displayName
                      ? `${m.displayName} (${m.username})`
                      : m.username;
                    const initial = (m.displayName ?? m.username).charAt(0).toUpperCase();
                    return (
                      <Chip
                        key={m.username}
                        avatar={<Avatar sx={{ width: 24, height: 24 }}>{initial}</Avatar>}
                        label={label}
                        variant="outlined"
                        size="small"
                        component={token && m.userId ? Link : 'div'}
                        {...(token && m.userId ? { to: `/users/${m.userId}` } : {})}
                        clickable={Boolean(token && m.userId)}
                        onClick={!token || !m.userId
                          ? () => setSelectedDelegate(selectedDelegate?.username === m.username ? null : m)
                          : undefined}
                        aria-label={`${t('delegates.title')}: ${label}`}
                        sx={s.delegateChip}
                      />
                    );
                  })}
                </Box>
              </Box>
            ))}

            <AnimatePresence>
              {selectedDelegate && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.9 }}
                  animate={{ opacity: 1, scale: 1 }}
                  exit={{ opacity: 0, scale: 0.9 }}
                  style={s.discordPopup}
                >
                  <Box sx={s.discordBox}>
                    <Typography variant="body2" sx={s.discordName}>
                      {selectedDelegate.displayName ?? selectedDelegate.username}
                    </Typography>
                    {selectedDelegate.displayName && (
                      <Typography variant="caption" sx={{ opacity: 0.7 }}>
                        @{selectedDelegate.username}
                      </Typography>
                    )}
                    {token && selectedDelegate.discord && (
                      <Typography variant="caption" sx={s.discordHandle}>
                        {selectedDelegate.discord}
                      </Typography>
                    )}
                    <Typography variant="caption" sx={{ opacity: 0.7 }}>
                      {t('delegates.since')} {formatDate(selectedDelegate.startDate, i18n.language)}
                    </Typography>
                  </Box>
                </motion.div>
              )}
            </AnimatePresence>
          </GlassCard>
          </Box>
        </Grid>
      </Grid>
    </motion.section>
  );
}
