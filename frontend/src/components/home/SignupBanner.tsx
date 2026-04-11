import { Box, Typography, Button } from '@mui/material';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { useAuthStore } from '@/stores/useAuthStore';
import { DISCORD_OAUTH_URL } from '@/lib/constants';
import GlassCard from '@/components/ui/GlassCard';
import * as s from './SignupBanner.styles';

export default function SignupBanner() {
  const { t } = useTranslation();
  const { token } = useAuthStore();

  if (token) return null;

  return (
    <motion.section initial={{ opacity: 0, y: 20 }} whileInView={{ opacity: 1, y: 0 }} viewport={{ once: true }}>
      <Box sx={s.wrapper}>
        <GlassCard sx={s.card}>
          <Box sx={s.gradientBar} />
          <Box sx={s.row}>
            <Box sx={s.textCol}>
              <Typography sx={s.title}>{t('signup.title')}</Typography>
              <Typography sx={s.subtitle}>{t('signup.subtitle')}</Typography>
            </Box>
            <Box sx={s.actions}>
              <Button variant="contained" href={DISCORD_OAUTH_URL} sx={s.primaryCta}>
                {t('signup.cta')}
              </Button>
              <Box
                component="a"
                href="https://discord.gg/5mYdsDSKk9"
                target="_blank"
                rel="noopener noreferrer"
                sx={s.discordLink}
              >
                💬 Discord ISFCE →
              </Box>
            </Box>
          </Box>
        </GlassCard>
      </Box>
    </motion.section>
  );
}
