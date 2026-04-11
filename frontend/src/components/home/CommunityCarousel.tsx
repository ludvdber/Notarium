import { useState } from 'react';
import { Box, Typography, Avatar, IconButton } from '@mui/material';
import { GitHub, LinkedIn, Close } from '@mui/icons-material';
import { Coffee } from 'lucide-react';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import { getFeaturedProfiles } from '@/api/endpoints';
import type { ProfileCardResponse } from '@/types';
import GlassCard from '@/components/ui/GlassCard';
import Badge from '@/components/ui/Badge';
import * as s from './CommunityCarousel.styles';

export default function CommunityCarousel() {
  const { t } = useTranslation();
  const { data: profiles } = useQuery({ queryKey: ['featured-profiles'], queryFn: getFeaturedProfiles });
  const [selected, setSelected] = useState<ProfileCardResponse | null>(null);

  const hasProfiles = (profiles?.length ?? 0) > 0;
  const doubled = hasProfiles ? [...profiles!, ...profiles!] : [];

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={s.section}>
        <Typography variant="h5" sx={s.title}>
          🌍 {t('community.carousel')}
        </Typography>

        {!hasProfiles && (
          <GlassCard sx={s.emptyCard}>
            <Typography sx={s.emptyIcon}>👥</Typography>
            <Typography variant="body1" color="text.secondary" sx={s.emptyText}>
              {t('community.empty')}
            </Typography>
          </GlassCard>
        )}

        {hasProfiles && (
          <Box sx={s.marqueeViewport}>
            <Box sx={s.fadeEdgeLeft} />
            <Box sx={s.fadeEdgeRight} />
            <Box sx={s.marqueeTrack}>
              {doubled.map((profile, i) => (
                <Box
                  key={`${profile.username}-${i}`}
                  onClick={() => setSelected(profile)}
                  sx={s.profileWrapper}
                >
                  <GlassCard sx={s.profileCard}>
                    <Avatar sx={s.profileAvatar}>{profile.username.charAt(0).toUpperCase()}</Avatar>
                    <Typography variant="body2" sx={s.profileName}>
                      {profile.username}
                    </Typography>
                    <Box sx={s.profileMeta}>
                      {profile.supporter && <Coffee size={14} color="#ffd93d" />}
                      {profile.discord && <Box sx={s.discordPill}>Discord</Box>}
                    </Box>
                    {profile.badges.length > 0 && (
                      <Box sx={s.profileBadges}>
                        {profile.badges.slice(0, 2).map((b) => (
                          <Badge key={b} label={b} />
                        ))}
                      </Box>
                    )}
                  </GlassCard>
                </Box>
              ))}
            </Box>
          </Box>
        )}

        <AnimatePresence>
          {selected && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: 20 }}
              style={s.popupWrapper}
            >
              <GlassCard sx={s.popupCard}>
                <IconButton size="small" onClick={() => setSelected(null)} sx={s.popupClose}>
                  <Close fontSize="small" />
                </IconButton>

                <Box sx={s.popupBody}>
                  <Avatar sx={s.popupAvatar}>{selected.username.charAt(0).toUpperCase()}</Avatar>
                  <Typography variant="h6" sx={s.popupName}>
                    {selected.username}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {selected.role}
                  </Typography>

                  <Box sx={s.popupActions}>
                    {selected.discord && <Box sx={s.popupDiscordPill}>{selected.discord}</Box>}
                    {selected.github && (
                      <IconButton component="a" href={`https://github.com/${selected.github}`} target="_blank" size="small">
                        <GitHub fontSize="small" />
                      </IconButton>
                    )}
                    {selected.linkedin && (
                      <IconButton component="a" href={selected.linkedin} target="_blank" size="small">
                        <LinkedIn fontSize="small" />
                      </IconButton>
                    )}
                  </Box>

                  {selected.badges.length > 0 && (
                    <Box sx={s.popupBadges}>
                      {selected.badges.map((b) => (
                        <Badge key={b} label={b} />
                      ))}
                    </Box>
                  )}
                </Box>
              </GlassCard>
            </motion.div>
          )}
        </AnimatePresence>
      </Box>
    </motion.section>
  );
}
