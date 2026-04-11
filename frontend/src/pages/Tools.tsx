import { useState } from 'react';
import { Typography, Tabs, Tab, Container } from '@mui/material';
import { Lan, SwapHoriz } from '@mui/icons-material';
import { useTranslation } from 'react-i18next';
import { motion, AnimatePresence } from 'framer-motion';
import GlassCard from '@/components/ui/GlassCard';
import IPv4Calculator from '@/components/tools/IPv4Calculator';
import BaseConverter from '@/components/tools/BaseConverter';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';
import * as s from './Tools.styles';

export default function Tools() {
  const { t } = useTranslation();
  const [tab, setTab] = useState(0);

  return (
    <>
      <Navbar />
      <Container maxWidth="md" sx={s.container}>
        <Typography variant="h4" sx={s.title}>
          {t('tools.title')}
        </Typography>

        <GlassCard sx={s.tabsCard}>
          <Tabs value={tab} onChange={(_, v) => setTab(v)} variant="fullWidth" sx={s.tabs}>
            <Tab icon={<Lan sx={s.tabIcon} />} iconPosition="start" label={t('tools.ipv4.tab')} />
            <Tab icon={<SwapHoriz sx={s.tabIcon} />} iconPosition="start" label={t('tools.base.tab')} />
          </Tabs>
        </GlassCard>

        <AnimatePresence mode="wait">
          <motion.div
            key={tab}
            initial={{ opacity: 0, x: tab === 0 ? -20 : 20 }}
            animate={{ opacity: 1, x: 0 }}
            exit={{ opacity: 0, x: tab === 0 ? 20 : -20 }}
            transition={{ duration: 0.25 }}
          >
            {tab === 0 ? <IPv4Calculator /> : <BaseConverter />}
          </motion.div>
        </AnimatePresence>
      </Container>
      <Footer />
    </>
  );
}
