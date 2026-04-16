import { useState, lazy, Suspense } from 'react';
import { Box, Typography, Button, Container, TextField, InputAdornment } from '@mui/material';
import { Search, Explore, CloudUpload, KeyboardArrowDown } from '@mui/icons-material';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import { useThemeStore } from '@/stores/useThemeStore';

const HeroBackground = lazy(() => import('@/components/three/ParticleField'));
import * as s from './HeroSection.styles';

export default function HeroSection() {
  const { t } = useTranslation();
  const theme = useThemeStore((st) => st.theme);
  const navigate = useNavigate();
  const [query, setQuery] = useState('');

  const handleSearch = () => {
    if (query.trim()) {
      navigate(`/browse?q=${encodeURIComponent(query.trim())}`);
    }
  };

  const handleScrollDown = () => {
    const hero = document.getElementById('hero-section');
    if (hero) {
      const next = hero.nextElementSibling;
      if (next) {
        next.scrollIntoView({ behavior: 'smooth', block: 'start' });
        return;
      }
    }
    window.scrollTo({ top: window.innerHeight, behavior: 'smooth' });
  };

  return (
    <motion.section initial="hidden" animate="show" variants={s.staggerVariants}>
      <Box id="hero-section" sx={s.heroContainer}>
        <Suspense fallback={null}>
          <HeroBackground theme={theme} />
        </Suspense>
        <Container maxWidth="md" sx={s.inner}>
          <motion.div variants={s.fadeUpVariants}>
            <Typography variant="h1" sx={s.title}>
              {t('hero.title')}
              <Box component="br" />
              <Box component="span" sx={s.titleGradient}>
                {t('hero.titleHighlight')}
              </Box>
            </Typography>
          </motion.div>

          <motion.div variants={s.fadeUpVariants}>
            <Typography variant="h6" color="text.secondary" sx={s.subtitle}>
              {t('hero.subtitle')}
            </Typography>
            <Typography
              variant="body2"
              sx={{
                mt: 1.5,
                display: 'inline-block',
                letterSpacing: 0.5,
                fontWeight: 600,
                fontSize: 13,
                px: 2,
                py: 0.5,
                borderRadius: 2,
                bgcolor: (th) => th.palette.mode === 'dark' ? 'rgba(0,210,255,0.08)' : 'rgba(0,210,255,0.12)',
                border: (th) => `1px solid ${th.palette.mode === 'dark' ? 'rgba(0,210,255,0.2)' : 'rgba(0,210,255,0.3)'}`,
                color: 'primary.main',
              }}
            >
              {t('hero.restricted')}
            </Typography>
          </motion.div>

          <motion.div variants={s.fadeUpVariants}>
            <TextField
              fullWidth
              placeholder={t('search.placeholder')}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              slotProps={{
                input: {
                  startAdornment: (
                    <InputAdornment position="start">
                      <Search sx={s.searchIcon} />
                    </InputAdornment>
                  ),
                },
              }}
              sx={s.searchField(theme)}
            />
          </motion.div>

          <motion.div variants={s.fadeUpVariants}>
            <Box sx={s.ctaRow}>
              <Button
                variant="contained"
                size="large"
                component={Link}
                to="/browse"
                startIcon={<Explore />}
                sx={s.ctaPrimary}
              >
                {t('hero.cta')}
              </Button>
              <Button
                variant="outlined"
                size="large"
                component={Link}
                to="/upload"
                startIcon={<CloudUpload />}
                sx={s.ctaSecondary}
              >
                {t('hero.ctaSecondary')}
              </Button>
            </Box>
          </motion.div>
        </Container>

        <Box
          component="button"
          type="button"
          onClick={handleScrollDown}
          aria-label={t('hero.scrollDown')}
          sx={s.scrollIndicator}
        >
          <Typography component="span" sx={s.scrollIndicatorLabel}>
            {t('hero.scrollDown')}
          </Typography>
          <motion.div
            animate={{ y: [0, 8, 0] }}
            transition={{ duration: 1.8, repeat: Infinity, ease: 'easeInOut' }}
            style={{ display: 'flex' }}
          >
            <KeyboardArrowDown fontSize="medium" />
          </motion.div>
        </Box>
      </Box>
    </motion.section>
  );
}
