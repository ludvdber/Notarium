import { Typography, Tabs, Tab } from '@mui/material';
import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import PageWrapper from '@/components/layout/PageWrapper';
import GlassCard from '@/components/ui/GlassCard';

export default function Admin() {
  const { t } = useTranslation();
  const [tab, setTab] = useState(0);

  return (
    <PageWrapper>
      <Typography variant="h4" sx={{ fontWeight: 700, mb: 3 }}>{t('nav.admin')}</Typography>
      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 3 }}>
        <Tab label={t('admin.tabs.documents')} />
        <Tab label={t('admin.tabs.courses')} />
        <Tab label={t('admin.tabs.professors')} />
        <Tab label={t('admin.tabs.reports')} />
      </Tabs>

      <GlassCard sx={{ p: 3 }}>
        <Typography variant="body1" color="text.secondary">
          {t('admin.placeholder')}
        </Typography>
      </GlassCard>
    </PageWrapper>
  );
}
