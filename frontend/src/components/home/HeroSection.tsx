import { useState } from 'react';
import { Box, Typography, Button, Container, TextField, InputAdornment } from '@mui/material';
import { Search, Explore, CloudUpload } from '@mui/icons-material';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { motion } from 'framer-motion';
import HeroBackground from '@/components/three/ParticleField';
import { useThemeStore } from '@/stores/useThemeStore';
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

  return (
    <motion.section initial="hidden" animate="show" variants={s.staggerVariants}>
      <Box sx={s.heroContainer}>
        <HeroBackground theme={theme} />
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
      </Box>
    </motion.section>
  );
}
