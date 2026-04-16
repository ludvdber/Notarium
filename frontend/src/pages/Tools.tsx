import { useState } from 'react';
import { Typography, Tabs, Tab, Container } from '@mui/material';
import { Lan, SwapHoriz, Code } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { Helmet } from 'react-helmet-async';
import { motion, AnimatePresence } from 'framer-motion';
import GlassCard from '@/components/ui/GlassCard';
import IPv4Calculator from '@/components/tools/IPv4Calculator';
import BaseConverter from '@/components/tools/BaseConverter';
import Base64Converter from '@/components/tools/Base64Converter';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import * as s from './Tools.styles';

export default function Tools() {
  const { t } = useTranslation();
  const [tab, setTab] = useState(0);

  return (
    <>
      <Helmet><title>{t('tools.title')} — Freenote</title></Helmet>
      <Navbar />
      <Container maxWidth="md" sx={s.container}>
        <Typography variant="h4" sx={s.title}>
          {t('tools.title')}
        </Typography>

        <GlassCard sx={s.tabsCard}>
          <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="fullWidth" sx={s.tabs}>
            <Tab icon={<Lan sx={s.tabIcon} />} iconPosition="start" label={t('tools.ipv4.tab')} />
            <Tab icon={<SwapHoriz sx={s.tabIcon} />} iconPosition="start" label={t('tools.base.tab')} />
            <Tab icon={<Code sx={s.tabIcon} />} iconPosition="start" label={t('tools.base64.tab')} />
          </Tabs>
        </GlassCard>

        <AnimatePresence mode="wait">
          <motion.div
            key={tab}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -10 }}
            transition={{ duration: 0.25 }}
          >
            {tab === 0 && <IPv4Calculator />}
            {tab === 1 && <BaseConverter />}
            {tab === 2 && <Base64Converter />}
          </motion.div>
        </AnimatePresence>
      </Container>
      <Footer />
    </>
  );
}
