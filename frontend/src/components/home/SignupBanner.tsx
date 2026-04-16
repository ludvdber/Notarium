import { useState, useEffect } from 'react';
import { Box, Typography, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuthStore } from '@/stores/useAuthStore';
import { DISCORD_OAUTH_URL, DISCORD_INVITE_URL } from '@/lib/constants';
import GlassCard from '@/components/ui/GlassCard';
import * as s from './SignupBanner.styles';

export default function SignupBanner() {
  const { t } = useTranslation();
  const { token } = useAuthStore();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    if (token) return;
    const onScroll = () => setScrolled(window.scrollY > window.innerHeight * 0.6);
    onScroll();
    window.addEventListener('scroll', onScroll, { passive: true });
    return () => window.removeEventListener('scroll', onScroll);
  }, [token]);

  return (
    <AnimatePresence>
      {!token && (
        <motion.section
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: -10 }}
          transition={{ duration: 0.3 }}
        >
          <Box sx={s.wrapper}>
            <GlassCard sx={s.card}>
              <Box sx={s.gradientBar} />
              <Box sx={s.row}>
                <Box sx={s.textCol}>
                  <Typography sx={s.title}>{t('signup.title')}</Typography>
                  <Typography sx={s.subtitle}>{t('signup.subtitle')}</Typography>
                  <Typography variant="caption" sx={{ opacity: 0.7, mt: 0.5, display: 'block' }}>
                    {t('community.discordRoles')}
                  </Typography>
                </Box>
                <Box sx={s.actions}>
                  <Button variant="contained" href={DISCORD_OAUTH_URL} sx={s.primaryCta}>
                    {t('signup.cta')}
                  </Button>
                  <Box
                    component="a"
                    href={DISCORD_INVITE_URL}
                    target="_blank"
                    rel="noopener noreferrer"
                    aria-label={t('signup.discordISFCE')}
                    sx={s.discordLink}
                  >
                    {t('signup.discordLink')}
                  </Box>
                </Box>
              </Box>
            </GlassCard>
          </Box>

          <AnimatePresence>
            {scrolled && (
              <motion.div
                initial={{ y: 80, opacity: 0 }}
                animate={{ y: 0, opacity: 1 }}
                exit={{ y: 80, opacity: 0 }}
                transition={{ duration: 0.25 }}
              >
                <Box sx={s.stickyBar}>
                  <Typography sx={s.stickyLabel}>{t('signup.stickyLabel')}</Typography>
                  <Button variant="contained" href={DISCORD_OAUTH_URL} sx={s.stickyCta}>
                    {t('signup.cta')}
                  </Button>
                </Box>
              </motion.div>
            )}
          </AnimatePresence>
        </motion.section>
      )}
    </AnimatePresence>
  );
}
